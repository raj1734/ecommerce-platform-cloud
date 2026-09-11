package com.ecommerce.inventory.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.List;

/**
 * Reserve inventory request (LLD §13.2).
 */
@Data
public class ReservationRequest {
    @NotBlank(message = "orderId is required")
    private String orderId;

    @NotEmpty(message = "items cannot be empty")
    @Valid
    private List<ReservationItem> items;

    @Data
    public static class ReservationItem {
        @NotBlank(message = "productId is required")
        private String productId;

        @NotNull(message = "quantity is required")
        @Positive(message = "quantity must be positive")
        private Integer quantity;
    }
}
