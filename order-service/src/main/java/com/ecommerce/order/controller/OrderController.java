package com.ecommerce.order.controller;

import com.ecommerce.order.dto.CheckoutRequest;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for Cart & Order operations.
 *
 * Endpoints:
 *
 * POST /api/v1/orders/checkout
 * GET  /api/v1/orders/{orderId}
 * GET  /api/v1/orders
 * POST /api/v1/orders/{orderId}/cancel
 */
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * Checkout an active cart and create an order.
     *
     * Required headers:
     * - X-User-Id
     * - X-User-Email (optional)
     * - Idempotency-Key (optional but recommended)
     * - X-Correlation-ID (optional; normally supplied by Gateway)
     */
    @PostMapping("/checkout")
    public ResponseEntity<OrderResponse> checkout(
            @Valid @RequestBody CheckoutRequest request,
            @RequestHeader("X-User-Id") UUID userId,
            @RequestHeader(value = "X-User-Email", required = false)
            String userEmail,
            @RequestHeader(value = "Idempotency-Key", required = false)
            String idempotencyKey,
            @RequestHeader(value = "X-Correlation-ID", required = false)
            String correlationId) {

        /*
         * The Gateway normally creates and propagates the correlation ID.
         * Generate one here as a fallback for direct/local service calls.
         */
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        OrderResponse response = orderService.checkout(
                request,
                userId,
                userEmail,
                idempotencyKey,
                correlationId
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * Get a single order belonging to the authenticated user.
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(
            @PathVariable UUID orderId,
            @RequestHeader("X-User-Id") UUID userId) {

        OrderResponse response = orderService.getOrder(
                orderId,
                userId
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Get orders belonging to the authenticated user.
     */
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getUserOrders(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Order.OrderStatus status) {

        Pageable pageable = PageRequest.of(page, size);

        List<OrderResponse> orders =
                orderService.getUserOrders(
                        userId,
                        status,
                        pageable
                );

        return ResponseEntity.ok(orders);
    }

    /**
     * Cancel an order belonging to the authenticated user.
     *
     * The current OrderService implementation accepts only:
     * cancelOrder(UUID orderId, UUID userId)
     *
     * Therefore the Idempotency-Key is accepted at the API boundary
     * but is not currently passed to the service layer.
     */
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(
            @PathVariable UUID orderId,
            @RequestHeader("X-User-Id") UUID userId,
            @RequestHeader(value = "Idempotency-Key", required = false)
            String idempotencyKey) {

        OrderResponse response = orderService.cancelOrder(
                orderId,
                userId
        );

        return ResponseEntity.ok(response);
    }
}