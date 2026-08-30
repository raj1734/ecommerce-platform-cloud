package com.ecommerce.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request payload for updating the current user's profile (LLD §11.2).
 */
@Data
public class UpdateProfileRequest {
    @NotBlank(message = "First name is required")
    private String firstName;

    private String lastName;

    private String phone;
}
