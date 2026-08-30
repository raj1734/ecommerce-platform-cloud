package com.ecommerce.inventory.service;

import com.ecommerce.inventory.dto.InventoryAdjustmentRequest;
import com.ecommerce.inventory.dto.InventoryAdjustmentResponse;
import com.ecommerce.inventory.dto.InventoryResponse;
import com.ecommerce.inventory.dto.InventoryUpsertRequest;
import com.ecommerce.inventory.dto.ReservationRequest;
import com.ecommerce.inventory.dto.ReservationResponse;
import com.ecommerce.inventory.entity.Inventory;
import com.ecommerce.inventory.entity.InventoryAdjustment;
import com.ecommerce.inventory.entity.InventoryReservation;
import com.ecommerce.inventory.event.OrderCreatedEvent;
import com.ecommerce.inventory.exception.InsufficientInventoryException;
import com.ecommerce.inventory.exception.InventoryNotFoundException;
import com.ecommerce.inventory.repository.InventoryAdjustmentRepository;
import com.ecommerce.inventory.repository.InventoryRepository;
import com.ecommerce.inventory.repository.InventoryReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryReservationRepository reservationRepository;
    private final InventoryAdjustmentRepository adjustmentRepository;

    @Transactional
    public InventoryResponse upsert(InventoryUpsertRequest request) {
        Inventory inventory = inventoryRepository.findByProductId(request.getProductId())
                .orElseGet(Inventory::new);
        inventory.setProductId(request.getProductId());
        inventory.setSku(request.getSku());
        inventory.setAvailableQuantity(request.getAvailableQuantity());
        if (inventory.getReservedQuantity() == null) inventory.setReservedQuantity(0);
        inventory.setReorderLevel(request.getReorderLevel());
        refreshStatus(inventory);
        return toResponse(inventoryRepository.save(inventory));
    }

    @Transactional(readOnly = true)
    public List<InventoryResponse> list() {
        return inventoryRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public InventoryResponse checkAvailability(String productId) {
        return toResponse(requireInventory(productId));
    }

    @Transactional
    public InventoryResponse adjust(String productId, InventoryAdjustmentRequest request, String performedBy) {
        Inventory inventory = requireInventory(productId);
        int before = safe(inventory.getAvailableQuantity());
        int delta = signedDelta(request);
        long afterLong = (long) before + delta;

        if (afterLong < 0) {
            throw new InsufficientInventoryException(
                    "Adjustment would make available stock negative for productId: " + productId);
        }

        int after = (int) afterLong;
        inventory.setAvailableQuantity(after);
        refreshStatus(inventory);
        inventoryRepository.save(inventory);

        InventoryAdjustment adjustment = new InventoryAdjustment();
        adjustment.setProductId(inventory.getProductId());
        adjustment.setSku(inventory.getSku());
        adjustment.setAdjustmentType(request.getAdjustmentType());
        adjustment.setQuantity(request.getQuantity());
        adjustment.setQuantityBefore(before);
        adjustment.setQuantityAfter(after);
        adjustment.setReason(request.getReason().trim());
        adjustment.setPerformedBy(performedBy);
        adjustmentRepository.save(adjustment);

        log.info("Inventory adjusted productId={} type={} quantity={} before={} after={} by={}",
                productId, request.getAdjustmentType(), request.getQuantity(), before, after, performedBy);
        return toResponse(inventory);
    }

    @Transactional(readOnly = true)
    public List<InventoryAdjustmentResponse> history(String productId) {
        requireInventory(productId);
        return adjustmentRepository.findTop50ByProductIdOrderByCreatedAtDesc(productId)
                .stream()
                .map(this::toAdjustmentResponse)
                .toList();
    }

    private int signedDelta(InventoryAdjustmentRequest request) {
        return switch (request.getAdjustmentType()) {
            case STOCK_RECEIVED, CORRECTION_INCREASE -> request.getQuantity();
            case DAMAGED, LOST, CORRECTION_DECREASE -> -request.getQuantity();
        };
    }

    @Transactional
    public ReservationResponse reserve(ReservationRequest request, String idempotencyKey) {
        var existing = reservationRepository.findByOrderId(request.getOrderId()).stream()
                .filter(r -> r.getStatus() == InventoryReservation.ReservationStatus.RESERVED)
                .findFirst();
        if (existing.isPresent()) {
            log.info("Idempotent reserve replay for orderId={}", request.getOrderId());
            return toResponse(existing.get());
        }

        InventoryReservation lastReservation = null;
        java.util.List<InventoryReservation> reservations = new java.util.ArrayList<>();
        for (ReservationRequest.ReservationItem item : request.getItems()) {
            Inventory inventory = requireInventory(item.getProductId());
            if (inventory.getAvailableQuantity() < item.getQuantity()) {
                throw new InsufficientInventoryException(
                        "Insufficient stock for productId: " + item.getProductId());
            }
            inventory.setAvailableQuantity(inventory.getAvailableQuantity() - item.getQuantity());
            inventory.setReservedQuantity(safe(inventory.getReservedQuantity()) + item.getQuantity());
            refreshStatus(inventory);
            inventoryRepository.save(inventory);

            InventoryReservation reservation = new InventoryReservation();
            reservation.setOrderId(request.getOrderId());
            reservation.setProductId(item.getProductId());
            reservation.setQuantity(item.getQuantity());
            reservation.setStatus(InventoryReservation.ReservationStatus.RESERVED);
            lastReservation = reservationRepository.save(reservation);
            reservations.add(lastReservation);
        }
        log.info("Reserved stock for orderId={} (key={})", request.getOrderId(), idempotencyKey);
        return toResponse(lastReservation, reservations);
    }

    @Transactional
    public ReservationResponse release(UUID reservationId) {
        InventoryReservation reservation = requireReservation(reservationId);
        if (reservation.getStatus() == InventoryReservation.ReservationStatus.RESERVED) {
            Inventory inventory = requireInventory(reservation.getProductId());
            inventory.setReservedQuantity(Math.max(0, safe(inventory.getReservedQuantity()) - reservation.getQuantity()));
            inventory.setAvailableQuantity(safe(inventory.getAvailableQuantity()) + reservation.getQuantity());
            refreshStatus(inventory);
            inventoryRepository.save(inventory);
            reservation.setStatus(InventoryReservation.ReservationStatus.RELEASED);
            reservationRepository.save(reservation);
        }
        log.info("Released reservation {}", reservationId);
        return toResponse(reservation);
    }

    @Transactional
    public ReservationResponse consume(UUID reservationId) {
        InventoryReservation reservation = requireReservation(reservationId);
        if (reservation.getStatus() == InventoryReservation.ReservationStatus.RESERVED) {
            Inventory inventory = requireInventory(reservation.getProductId());
            inventory.setReservedQuantity(Math.max(0, safe(inventory.getReservedQuantity()) - reservation.getQuantity()));
            refreshStatus(inventory);
            inventoryRepository.save(inventory);
            reservation.setStatus(InventoryReservation.ReservationStatus.CONSUMED);
            reservationRepository.save(reservation);
        }
        log.info("Consumed reservation {}", reservationId);
        return toResponse(reservation);
    }

    @Transactional
    public void updateStockForOrder(OrderCreatedEvent event) {
        if (event == null || event.getOrderId() == null) {
            log.warn("Received null or invalid OrderCreatedEvent; skipping inventory update");
            return;
        }
        reservationRepository.findByOrderId(String.valueOf(event.getOrderId())).stream()
                .filter(r -> r.getStatus() == InventoryReservation.ReservationStatus.RESERVED)
                .forEach(r -> consume(r.getId()));
        log.info("Processed stock update for orderId={} (userId={})", event.getOrderId(), event.getUserId());
    }

    @Transactional
    public void releaseByOrder(String orderId) {
        reservationRepository.findByOrderId(orderId).stream()
                .filter(r -> r.getStatus() == InventoryReservation.ReservationStatus.RESERVED)
                .forEach(r -> release(r.getId()));
    }

    private Inventory requireInventory(String productId) {
        return inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new InventoryNotFoundException("Inventory not found for productId: " + productId));
    }

    private InventoryReservation requireReservation(UUID reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new InventoryNotFoundException("Reservation not found with id: " + reservationId));
    }

    private InventoryResponse toResponse(Inventory inventory) {
        return InventoryResponse.builder()
                .productId(inventory.getProductId())
                .sku(inventory.getSku())
                .availableQuantity(safe(inventory.getAvailableQuantity()))
                .reservedQuantity(safe(inventory.getReservedQuantity()))
                .totalQuantity(safe(inventory.getAvailableQuantity()) + safe(inventory.getReservedQuantity()))
                .reorderLevel(safe(inventory.getReorderLevel()))
                .status(inventory.getStatus().name())
                .build();
    }

    private InventoryAdjustmentResponse toAdjustmentResponse(InventoryAdjustment adjustment) {
        return InventoryAdjustmentResponse.builder()
                .id(adjustment.getId())
                .productId(adjustment.getProductId())
                .sku(adjustment.getSku())
                .adjustmentType(adjustment.getAdjustmentType())
                .quantity(adjustment.getQuantity())
                .quantityBefore(adjustment.getQuantityBefore())
                .quantityAfter(adjustment.getQuantityAfter())
                .reason(adjustment.getReason())
                .performedBy(adjustment.getPerformedBy())
                .createdAt(adjustment.getCreatedAt())
                .build();
    }

    private void refreshStatus(Inventory inventory) {
        int available = safe(inventory.getAvailableQuantity());
        int reorder = safe(inventory.getReorderLevel());
        if (inventory.getStatus() == Inventory.InventoryStatus.DISCONTINUED) return;
        if (available == 0) inventory.setStatus(Inventory.InventoryStatus.OUT_OF_STOCK);
        else if (available <= reorder) inventory.setStatus(Inventory.InventoryStatus.LOW_STOCK);
        else inventory.setStatus(Inventory.InventoryStatus.AVAILABLE);
    }

    private int safe(Integer value) {
        return value == null ? 0 : value;
    }

    private ReservationResponse toResponse(InventoryReservation reservation) {
        return toResponse(reservation, java.util.List.of(reservation));
    }

    private ReservationResponse toResponse(InventoryReservation reservation, java.util.List<InventoryReservation> reservations) {
        return ReservationResponse.builder()
                .reservationId(String.valueOf(reservation.getId()))
                .orderId(reservation.getOrderId())
                .status(reservation.getStatus().name())
                .reservationIds(reservations.stream().map(r -> String.valueOf(r.getId())).toList())
                .build();
    }
}
