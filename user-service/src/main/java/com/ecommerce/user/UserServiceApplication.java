package com.ecommerce.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * User Service application entry point.
 *
 * <p>Owns customer profile, address and preference data (LLD §3) and exposes the
 * self-service {@code /api/v1/users/me...} contract (LLD §11). Authentication
 * credentials remain in the Auth Service; {@code authUserId} is stored as a
 * reference only (no cross-database foreign key).</p>
 */
@SpringBootApplication
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
