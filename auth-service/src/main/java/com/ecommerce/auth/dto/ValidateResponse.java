package com.ecommerce.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Token validation response (LLD §10.3).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidateResponse {
    private boolean valid;
    private String userId;
    private List<String> roles;
}
