package com.ecommerce.catalog.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Standardized error response payload returned by the Catalog Service.
 *
 * <p>Aligned with the Low-Level Design exception-handling strategy for
 * consistent, structured error responses across the platform.</p>
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
