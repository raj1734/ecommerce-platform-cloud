package com.ecommerce.user.exception;

/**
 * Thrown when a requested user-owned resource does not exist (LLD §18 RESOURCE_NOT_FOUND → 404).
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
