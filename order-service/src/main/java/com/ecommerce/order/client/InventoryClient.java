package com.ecommerce.order.client;

import com.ecommerce.order.dto.ReserveInventoryRequest;
import com.ecommerce.order.dto.ReserveInventoryResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * Feign client for synchronous communication with the Inventory Service.
 */
@FeignClient(
        name = "inventory-service",
        url = "${inventory.service.url:http://localhost:8086}"
)
public interface InventoryClient {

    /**
     * Reserve inventory for an order.
     *
     * @param request        inventory reservation details
     * @param idempotencyKey unique key to prevent duplicate reservations
     * @return reservation response
     */
    @PostMapping("/api/v1/inventory/reservations")
    ReserveInventoryResponse reserve(
            @RequestBody ReserveInventoryRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey
    );

    /**
     * Release all outstanding inventory reservations for an order.
     *
     * Inventory Service endpoint:
     * POST /api/v1/inventory/reservations/order/{orderId}/release
     *
     * @param orderId order ID
     */
    @PostMapping("/api/v1/inventory/reservations/order/{orderId}/release")
    void releaseByOrder(
            @PathVariable("orderId") String orderId
    );

    /**
     * Release a specific inventory reservation.
     *
     * @param reservationId reservation ID
     * @return updated reservation response
     */
    @PostMapping("/api/v1/inventory/reservations/{reservationId}/release")
    ReserveInventoryResponse release(
            @PathVariable("reservationId") String reservationId
    );

    /**
     * Consume a specific inventory reservation.
     *
     * @param reservationId reservation ID
     * @return updated reservation response
     */
    @PostMapping("/api/v1/inventory/reservations/{reservationId}/consume")
    ReserveInventoryResponse consume(
            @PathVariable("reservationId") String reservationId
    );
}