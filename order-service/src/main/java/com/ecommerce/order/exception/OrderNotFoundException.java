package com.ecommerce.order.exception;

/**
 * Thrown when a requested order cannot be found.
 *
 * <p>Maps to HTTP 404 NOT FOUND.</p>
 */
public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(String message) {
        super(message);
    }
}
