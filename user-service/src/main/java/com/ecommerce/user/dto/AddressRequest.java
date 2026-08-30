package com.ecommerce.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request payload for adding an address (LLD §11.4).
 */
@Data
public class AddressRequest {
    @NotBlank(message = "addressLine1 is required")
    private String addressLine1;

    private String addressLine2;

    @NotBlank(message = "city is required")
    private String city;

    @NotBlank(message = "state is required")
    private String state;

    @NotBlank(message = "postalCode is required")
    private String postalCode;

    @NotBlank(message = "country is required")
    private String country;

    private Boolean isDefault = false;
}
