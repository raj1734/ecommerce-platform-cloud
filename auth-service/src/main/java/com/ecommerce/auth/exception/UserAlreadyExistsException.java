package com.ecommerce.auth.exception;

/**
 * Thrown when a registration attempt uses an email that already exists.
 *
 * <p>Maps to HTTP 409 CONFLICT as specified in the LLD (Section 4.12).</p>
 */
public class UserAlreadyExistsException extends RuntimeException {

    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
