package com.ecommerce.inventory.dto;

import com.ecommerce.inventory.entity.InventoryAdjustment;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InventoryAdjustmentRequest {
    @NotNull
    private InventoryAdjustment.AdjustmentType adjustmentType;

    @NotNull
    @Min(1)
    private Integer quantity;

    @NotBlank
    private String reason;
}
