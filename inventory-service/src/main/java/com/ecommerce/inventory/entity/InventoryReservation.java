package com.ecommerce.inventory.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Inventory reservation entity (LLD §5.2 inventory_reservations).
 *
 * <p>{@code orderId} is a reference to the Order Service and is deliberately not a
 * database foreign key (Database-per-Service rule).</p>
 */
@Entity
@Table(name = "inventory_reservations",
        indexes = {
                @Index(name = "idx_res_order", columnList = "order_id"),
                @Index(name = "idx_res_product", columnList = "product_id"),
                @Index(name = "idx_res_status", columnList = "status"),
                @Index(name = "idx_res_expires", columnList = "expires_at")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "order_id", nullable = false, length = 100)
    private String orderId;

    @Column(name = "product_id", nullable = false, length = 100)
    private String productId;

    @Column(nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReservationStatus status = ReservationStatus.RESERVED;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum ReservationStatus {
        RESERVED, RELEASED, CONSUMED, EXPIRED
    }
}
