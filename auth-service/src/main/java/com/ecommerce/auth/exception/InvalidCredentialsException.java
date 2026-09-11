package com.ecommerce.auth.exception;

/**
 * Thrown when login credentials are invalid.
 *
 * <p>Maps to HTTP 401 UNAUTHORIZED as specified in the LLD (Section 4.12).</p>
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException(String message) {
        super(message);
    }
}
