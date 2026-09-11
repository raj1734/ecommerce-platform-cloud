package com.ecommerce.order.service;

import com.ecommerce.order.client.InventoryClient;
import com.ecommerce.order.client.UserClient;
import com.ecommerce.order.client.PaymentClient;
import com.ecommerce.order.config.KafkaProducerConfig;
import com.ecommerce.order.dto.*;
import com.ecommerce.order.entity.Cart;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.entity.OrderItem;
import com.ecommerce.order.entity.OrderStatusHistory;
import com.ecommerce.order.event.OrderCreatedEvent;
import com.ecommerce.order.exception.CartNotFoundException;
import com.ecommerce.order.exception.ForbiddenException;
import com.ecommerce.order.exception.OrderNotCancellableException;
import com.ecommerce.order.exception.OrderNotFoundException;
import com.ecommerce.order.repository.CartRepository;
import com.ecommerce.order.repository.OrderRepository;
import com.ecommerce.order.repository.OrderStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Cart &amp; Order Service business logic implementing the production-oriented
 * checkout flow described in LLD §15 and §21:
 *
 * <ol>
 *   <li>Validate cart</li>
 *   <li>Reserve inventory (Inventory Service, synchronous)</li>
 *   <li>Process payment (Payment Service, synchronous)</li>
 *   <li>Persist the order with snapshots + status history</li>
 *   <li>Publish {@code OrderCreated} to Kafka</li>
 * </ol>
 *
 * <p>Checkout is idempotent via the {@code Idempotency-Key} (LLD §9 / §15.1).</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private static final DateTimeFormatter ORDER_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository statusHistoryRepository;
    private final CartRepository cartRepository;
    private final InventoryClient inventoryClient;
    private final UserClient userClient;
    private final PaymentClient paymentClient;
    private final OrderDownstreamService downstreamService;
    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    @Transactional
    public OrderResponse checkout(CheckoutRequest request, UUID userId, String userEmail, String idempotencyKey, String correlationId) {
        // Idempotency guard (LLD §15.1): retried key returns the original order.
        if (idempotencyKey != null) {
            var existing = orderRepository.findByIdempotencyKey(idempotencyKey);
            if (existing.isPresent()) {
                log.info("Idempotent checkout replay for key={}", idempotencyKey);
                return toOrderResponse(existing.get());
            }
        }

        Cart cart = cartRepository.findById(request.getCartId())
                .orElseThrow(() -> new CartNotFoundException("Cart not found with id: " + request.getCartId()));
        if (!cart.getUserId().equals(userId)) {
            throw new CartNotFoundException("Cart not found for authenticated user");
        }
        if (cart.getItems().isEmpty()) {
            throw new CartNotFoundException("Cart is empty: " + request.getCartId());
        }
        // Revalidate product state and price at checkout; cart data is only a price snapshot.
        Map<String, ProductResponse> checkoutProducts = new HashMap<>();
        for (var cartItem : cart.getItems()) {
            ProductResponse product = downstreamService.getCatalogProduct(cartItem.getProductId());
            if (product == null || !"ACTIVE".equalsIgnoreCase(product.getStatus()) || product.getPrice() == null || product.resolvedPrice().compareTo(cartItem.getUnitPrice()) != 0) {
                throw new IllegalStateException("Product/price changed for productId: " + cartItem.getProductId());
            }
            checkoutProducts.put(cartItem.getProductId(), product);
        }

        Order order = new Order();
        order.setOrderNumber(generateOrderNumber());
        order.setUserId(userId);
        order.setUserEmail(userEmail == null ? "" : userEmail);
        order.setIdempotencyKey(idempotencyKey);
        order.setShippingAddressSnapshot(resolveShippingAddressSnapshot(request.getShippingAddressId(), userId));
        order.setStatus(Order.OrderStatus.PENDING);
        order.setPaymentStatus(Order.PaymentStatus.PENDING);

        BigDecimal total = BigDecimal.ZERO;
        for (var cartItem : cart.getItems()) {
            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProductId(cartItem.getProductId());
            item.setSku(cartItem.getSku());
            item.setProductName(checkoutProducts.get(cartItem.getProductId()).getName());
            item.setUnitPrice(cartItem.getUnitPrice());
            item.setQuantity(cartItem.getQuantity());
            BigDecimal subtotal = cartItem.getUnitPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            item.setSubtotal(subtotal);
            order.getItems().add(item);
            total = total.add(subtotal);
        }
        order.setTotalAmount(total);
        Order savedOrder = orderRepository.save(order);
        recordStatus(savedOrder, null, Order.OrderStatus.PENDING, "Order created");

        try {
            downstreamService.reserveInventory(savedOrder);
            PaymentClientResponse payment = downstreamService.processPayment(savedOrder);
            if ("SUCCESS".equalsIgnoreCase(payment.getStatus())) {
                savedOrder.setPaymentStatus(Order.PaymentStatus.SUCCESS);
                savedOrder.setPaymentId(payment.getPaymentId());
                updateStatus(savedOrder, Order.OrderStatus.CONFIRMED, "Payment successful");
            } else {
                savedOrder.setPaymentStatus(Order.PaymentStatus.FAILED);
                try {
                    inventoryClient.releaseByOrder(savedOrder.getId().toString());
                } catch (Exception releaseEx) {
                    log.error("Inventory release failed for order {}", savedOrder.getId(), releaseEx);
                }
                updateStatus(savedOrder, Order.OrderStatus.FAILED, "Payment failed");
            }
        } catch (Exception ex) {
            log.error("Checkout downstream failure for order {}: {}", savedOrder.getId(), ex.getMessage());
            savedOrder.setPaymentStatus(Order.PaymentStatus.FAILED);
            try {
                inventoryClient.releaseByOrder(savedOrder.getId().toString());
            } catch (Exception releaseEx) {
                log.error("Inventory release failed for order {}", savedOrder.getId(), releaseEx);
            }
            updateStatus(savedOrder, Order.OrderStatus.FAILED, "Downstream failure: " + ex.getMessage());
        }

        savedOrder = orderRepository.save(savedOrder);

        // Only consume the cart when checkout succeeds; failed payment/inventory can be retried.
        if (savedOrder.getStatus() == Order.OrderStatus.CONFIRMED) {
            cart.setStatus(Cart.CartStatus.CHECKED_OUT);
            cartRepository.save(cart);
        }

        if (savedOrder.getStatus() == Order.OrderStatus.CONFIRMED) {
            publishOrderCreatedEvent(savedOrder, correlationId);
        }
        return toOrderResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(UUID id, UUID userId) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + id));
        // Resource-ownership check (LLD §23).
        if (!order.getUserId().equals(userId)) {
            throw new ForbiddenException("You do not have access to this order");
        }
        return toOrderResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getUserOrders(UUID userId, Order.OrderStatus status, Pageable pageable) {
        Page<Order> page = (status == null)
                ? orderRepository.findByUserId(userId, pageable)
                : orderRepository.findByUserIdAndStatus(userId, status, pageable);
        return page.getContent().stream().map(this::toOrderResponse).collect(Collectors.toList());
    }

    @Transactional
    public OrderResponse cancelOrder(UUID id, UUID userId) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + id));
        if (!order.getUserId().equals(userId)) {
            throw new ForbiddenException("You do not have access to this order");
        }
        if (order.getStatus() == Order.OrderStatus.COMPLETED
                || order.getStatus() == Order.OrderStatus.CANCELLED) {
            throw new OrderNotCancellableException("Order cannot be cancelled in status: " + order.getStatus());
        }
        updateStatus(order, Order.OrderStatus.CANCELLED, "Cancelled by user");
        if (order.getPaymentStatus() == Order.PaymentStatus.SUCCESS && order.getPaymentId() != null) {
            try {
                paymentClient.refund(order.getPaymentId(), RefundClientRequest.builder().amount(order.getTotalAmount()).reason("ORDER_CANCELLED").build(), "refund-" + order.getId());
                order.setPaymentStatus(Order.PaymentStatus.REFUND_PENDING);
            } catch (Exception ex) {
                log.error("Refund initiation failed for order {}", order.getId(), ex);
                order.setPaymentStatus(Order.PaymentStatus.REFUND_PENDING);
            }
        }
        return toOrderResponse(orderRepository.save(order));
    }

    private String resolveShippingAddressSnapshot(String addressId, UUID userId) {
        try {
            var addresses = userClient.getAddresses(userId.toString());
            var selected = addressId == null ? addresses.stream().filter(a -> Boolean.TRUE.equals(a.getIsDefault())).findFirst() : addresses.stream().filter(a -> addressId.equals(a.getId().toString())).findFirst();
            if (selected.isEmpty()) throw new IllegalStateException("Shipping address not found");
            var a = selected.get();
            return String.format("{\"addressLine1\":\"%s\",\"addressLine2\":\"%s\",\"city\":\"%s\",\"state\":\"%s\",\"postalCode\":\"%s\",\"country\":\"%s\"}", safe(a.getAddressLine1()), safe(a.getAddressLine2()), safe(a.getCity()), safe(a.getState()), safe(a.getPostalCode()), safe(a.getCountry()));
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to resolve shipping address", ex);
        }
    }

    private String safe(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private void updateStatus(Order order, Order.OrderStatus newStatus, String reason) {
        Order.OrderStatus previous = order.getStatus();
        order.setStatus(newStatus);
        recordStatus(order, previous, newStatus, reason);
    }

    private void recordStatus(Order order, Order.OrderStatus previous, Order.OrderStatus newStatus, String reason) {
        OrderStatusHistory history = new OrderStatusHistory();
        history.setOrderId(order.getId());
        history.setPreviousStatus(previous != null ? previous.name() : null);
        history.setNewStatus(newStatus.name());
        history.setReason(reason);
        history.setChangedBy("system");
        statusHistoryRepository.save(history);
    }

    private void publishOrderCreatedEvent(Order order, String correlationId) {
        OrderCreatedEvent event = new OrderCreatedEvent(
                UUID.randomUUID().toString(),
                "OrderCreated",
                "1",
                order.getId(),
                order.getUserId(),
                order.getUserEmail(),
                order.getTotalAmount(),
                order.getCurrency(),
                order.getCreatedAt(),
                safeCorrelationId(correlationId)
        );
        kafkaTemplate.send(KafkaProducerConfig.ORDER_CREATED_TOPIC, event);
        log.info("Published OrderCreated event for order {}", order.getId());
    }

    private UUID safeCorrelationId(String correlationId) {
        try {
            return correlationId == null ? UUID.randomUUID() : UUID.fromString(correlationId);
        } catch (IllegalArgumentException ex) {
            return UUID.randomUUID();
        }
    }

    private String generateOrderNumber() {
        return "ORD-" + LocalDate.now().format(ORDER_DATE) + "-"
                + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private OrderResponse toOrderResponse(Order order) {
        List<OrderResponse.OrderItemResponse> items = order.getItems().stream()
                .map(i -> OrderResponse.OrderItemResponse.builder()
                        .productId(i.getProductId())
                        .productName(i.getProductName())
                        .quantity(i.getQuantity())
                        .unitPrice(i.getUnitPrice())
                        .subtotal(i.getSubtotal())
                        .build())
                .collect(Collectors.toList());
        return OrderResponse.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .status(order.getStatus().name())
                .paymentStatus(order.getPaymentStatus().name())
                .totalAmount(order.getTotalAmount())
                .currency(order.getCurrency())
                .items(items)
                .build();
    }
}
