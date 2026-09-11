package com.ecommerce.order.exception;

/**
 * Thrown when an order cannot be cancelled in its current state
 * (LLD §18 ORDER_NOT_CANCELLABLE → 422).
 */
public class OrderNotCancellableException extends RuntimeException {
    public OrderNotCancellableException(String message) {
        super(message);
    }
}
