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
 * Inventory entity (LLD §5.1 inventory). Uses optimistic locking to protect
 * concurrent stock reservation/consumption.
 */
@Entity
@Table(name = "inventory",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "product_id"),
                @UniqueConstraint(columnNames = "sku")
        },
        indexes = @Index(name = "idx_inventory_status", columnList = "status"))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "product_id", nullable = false, unique = true, length = 100)
    private String productId;

    @Column(nullable = false, unique = true, length = 100)
    private String sku;

    @Column(name = "available_quantity", nullable = false)
    private Integer availableQuantity = 0;

    @Column(name = "reserved_quantity", nullable = false)
    private Integer reservedQuantity = 0;

    @Column(name = "reorder_level", nullable = false)
    private Integer reorderLevel = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private InventoryStatus status = InventoryStatus.AVAILABLE;

    @Version
    @Column(nullable = false)
    private Long version;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum InventoryStatus {
        AVAILABLE, LOW_STOCK, OUT_OF_STOCK, DISCONTINUED
    }
}
