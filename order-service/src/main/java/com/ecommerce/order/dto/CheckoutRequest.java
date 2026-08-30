package com.ecommerce.order.dto;

import java.util.UUID;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Checkout request (LLD §15.1).
 */
@Data
public class CheckoutRequest {
    @NotNull(message = "cartId is required")
    private UUID cartId;

    private String shippingAddressId;

    private String paymentMethod = "CARD";
}
