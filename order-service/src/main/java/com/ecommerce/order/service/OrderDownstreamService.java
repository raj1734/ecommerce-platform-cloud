package com.ecommerce.order.service;

import com.ecommerce.order.client.CatalogClient;
import com.ecommerce.order.client.InventoryClient;
import com.ecommerce.order.client.PaymentClient;
import com.ecommerce.order.dto.PaymentClientRequest;
import com.ecommerce.order.dto.PaymentClientResponse;
import com.ecommerce.order.dto.ProductResponse;
import com.ecommerce.order.dto.ReserveInventoryRequest;
import com.ecommerce.order.entity.Order;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

/**
 * Resilience boundary for synchronous downstream calls made during checkout.
 * Kept in a separate Spring bean so Retry/CircuitBreaker AOP is actually applied.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderDownstreamService {

    private final CatalogClient catalogClient;
    private final InventoryClient inventoryClient;
    private final PaymentClient paymentClient;

    @CircuitBreaker(name = "catalogService", fallbackMethod = "catalogLookupFallback")
    @Retry(name = "catalogService")
    public ProductResponse getCatalogProduct(String productId) {
        return catalogClient.getProductById(productId);
    }

    public ProductResponse catalogLookupFallback(String productId, Throwable ex) {
        log.error("Catalog lookup fallback for product {}: {}", productId, ex.getMessage());
        throw new IllegalStateException("Catalog Service unavailable for product " + productId, ex);
    }

    @CircuitBreaker(name = "inventoryService", fallbackMethod = "reserveInventoryFallback")
    @Retry(name = "inventoryService")
    public void reserveInventory(Order order) {
        ReserveInventoryRequest req = ReserveInventoryRequest.builder()
                .orderId(String.valueOf(order.getId()))
                .items(order.getItems().stream()
                        .map(i -> ReserveInventoryRequest.Item.builder()
                                .productId(i.getProductId())
                                .quantity(i.getQuantity())
                                .build())
                        .collect(Collectors.toList()))
                .build();
        inventoryClient.reserve(req, "resv-" + order.getId());
        log.info("Reserved inventory for order {}", order.getId());
    }

    public void reserveInventoryFallback(Order order, Throwable ex) {
        log.error("Inventory reservation fallback for order {}: {}", order.getId(), ex.getMessage());
        throw new IllegalStateException("Inventory reservation unavailable", ex);
    }

    @CircuitBreaker(name = "paymentService", fallbackMethod = "processPaymentFallback")
    @Retry(name = "paymentService")
    public PaymentClientResponse processPayment(Order order) {
        PaymentClientRequest req = PaymentClientRequest.builder()
                .orderId(String.valueOf(order.getId()))
                .amount(order.getTotalAmount())
                .currency(order.getCurrency())
                .paymentMethod("CARD")
                .build();
        return paymentClient.pay(req, "pay-" + order.getId());
    }

    public PaymentClientResponse processPaymentFallback(Order order, Throwable ex) {
        log.error("Payment fallback for order {}: {}", order.getId(), ex.getMessage());
        PaymentClientResponse response = new PaymentClientResponse();
        response.setOrderId(String.valueOf(order.getId()));
        response.setStatus("FAILED");
        return response;
    }
}
