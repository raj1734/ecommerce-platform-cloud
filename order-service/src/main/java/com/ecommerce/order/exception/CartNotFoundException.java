package com.ecommerce.order.exception;

/**
 * Thrown when a cart or cart item cannot be found (LLD §18 RESOURCE_NOT_FOUND → 404).
 */
public class CartNotFoundException extends RuntimeException {
    public CartNotFoundException(String message) {
        super(message);
    }
}
