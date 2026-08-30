package com.ecommerce.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * Add cart item request (LLD §14.2).
 */
@Data
public class AddCartItemRequest {
    @NotBlank(message = "productId is required")
    private String productId;

    @NotNull(message = "quantity is required")
    @Positive(message = "quantity must be positive")
    private Integer quantity;
}
