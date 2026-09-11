package com.ecommerce.auth.exception;

/**
 * Thrown when a requested user cannot be found.
 *
 * <p>Maps to HTTP 404 NOT FOUND as specified in the LLD (Section 4.12).</p>
 */
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String message) {
        super(message);
    }
}
