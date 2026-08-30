package com.ecommerce.inventory.dto; import jakarta.validation.constraints.*; import lombok.Data;
@Data public class InventoryUpsertRequest { @NotBlank private String productId; @NotBlank private String sku; @NotNull @Min(0) private Integer availableQuantity; @NotNull @Min(0) private Integer reorderLevel; }
