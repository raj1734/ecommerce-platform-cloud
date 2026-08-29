package com.ecommerce.inventory.controller;

import com.ecommerce.inventory.dto.InventoryAdjustmentRequest;
import com.ecommerce.inventory.dto.InventoryAdjustmentResponse;
import com.ecommerce.inventory.dto.InventoryResponse;
import com.ecommerce.inventory.dto.InventoryUpsertRequest;
import com.ecommerce.inventory.dto.ReservationRequest;
import com.ecommerce.inventory.dto.ReservationResponse;
import com.ecommerce.inventory.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    public ResponseEntity<List<InventoryResponse>> list() {
        return ResponseEntity.ok(inventoryService.list());
    }

    @PostMapping
    public ResponseEntity<InventoryResponse> upsert(
            @RequestHeader(value = "X-User-Roles", required = false) String roles,
            @Valid @RequestBody InventoryUpsertRequest request) {
        requireAdmin(roles);
        return ResponseEntity.status(HttpStatus.CREATED).body(inventoryService.upsert(request));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<InventoryResponse> checkAvailability(@PathVariable String productId) {
        return ResponseEntity.ok(inventoryService.checkAvailability(productId));
    }

    @GetMapping("/{productId}/history")
    public ResponseEntity<List<InventoryAdjustmentResponse>> history(
            @RequestHeader(value = "X-User-Roles", required = false) String roles,
            @PathVariable String productId) {
        requireAdmin(roles);
        return ResponseEntity.ok(inventoryService.history(productId));
    }

    @PostMapping("/{productId}/adjustments")
    public ResponseEntity<InventoryResponse> adjust(
            @RequestHeader(value = "X-User-Roles", required = false) String roles,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @Valid @RequestBody InventoryAdjustmentRequest request,
            @PathVariable String productId) {
        requireAdmin(roles);
        return ResponseEntity.ok(inventoryService.adjust(productId, request, userId));
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> reserve(
            @Valid @RequestBody ReservationRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inventoryService.reserve(request, idempotencyKey));
    }

    @PostMapping("/reservations/{reservationId}/release")
    public ResponseEntity<ReservationResponse> release(@PathVariable UUID reservationId) {
        return ResponseEntity.ok(inventoryService.release(reservationId));
    }

    @PostMapping("/reservations/order/{orderId}/release")
    public ResponseEntity<Void> releaseByOrder(@PathVariable String orderId) {
        inventoryService.releaseByOrder(orderId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reservations/{reservationId}/consume")
    public ResponseEntity<ReservationResponse> consume(@PathVariable UUID reservationId) {
        return ResponseEntity.ok(inventoryService.consume(reservationId));
    }

    private void requireAdmin(String roles) {
        if (roles == null || java.util.Arrays.stream(roles.split(","))
                .map(String::trim)
                .noneMatch("ADMIN"::equalsIgnoreCase)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.FORBIDDEN, "ADMIN role required");
        }
    }
}
