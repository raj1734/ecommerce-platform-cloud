package com.ecommerce.inventory.exception;

/**
 * Thrown when there is not enough available stock to satisfy a reservation
 * (LLD §18 INSUFFICIENT_INVENTORY → 422).
 */
public class InsufficientInventoryException extends RuntimeException {
    public InsufficientInventoryException(String message) {
        super(message);
    }
}
