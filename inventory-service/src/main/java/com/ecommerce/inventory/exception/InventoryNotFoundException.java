package com.ecommerce.inventory.exception;

/**
 * Thrown when inventory or a reservation cannot be found (LLD §18 RESOURCE_NOT_FOUND → 404).
 */
public class InventoryNotFoundException extends RuntimeException {
    public InventoryNotFoundException(String message) {
        super(message);
    }
}
