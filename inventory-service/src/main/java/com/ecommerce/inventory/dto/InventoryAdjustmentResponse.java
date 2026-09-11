package com.ecommerce.inventory.dto;

import com.ecommerce.inventory.entity.InventoryAdjustment;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class InventoryAdjustmentResponse {
    private UUID id;
    private String productId;
    private String sku;
    private InventoryAdjustment.AdjustmentType adjustmentType;
    private Integer quantity;
    private Integer quantityBefore;
    private Integer quantityAfter;
    private String reason;
    private String performedBy;
    private LocalDateTime createdAt;
}
