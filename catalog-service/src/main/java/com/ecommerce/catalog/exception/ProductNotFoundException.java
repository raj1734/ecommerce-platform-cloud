package com.ecommerce.catalog.exception;

/**
 * Thrown when a requested product cannot be found.
 *
 * <p>Maps to HTTP 404 NOT FOUND.</p>
 */
public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(String message) {
        super(message);
    }
}
