package com.ecommerce.auth.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Standardized error response payload returned by the Auth Service.
 *
 * <p>Aligned with the Low-Level Design (Section 4.12 Exception Handling), which
 * mandates consistent, structured error responses for authentication failures.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private int status;
    private String error;
    private String message;
    private String path;
    private LocalDateTime timestamp;
}
