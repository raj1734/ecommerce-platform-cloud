package com.ecommerce.web.model;

import java.util.List;

public record SessionUser(
        String userId,
        String username,
        String email,
        String token,
        List<String> roles
) {
    public boolean admin() {
        if (roles == null || roles.isEmpty()) return false;

        return roles.stream()
                .filter(r -> r != null && !r.isBlank())
                .map(String::trim)
                .map(r -> r.startsWith("ROLE_") ? r.substring(5) : r)
                .anyMatch(r -> "ADMIN".equalsIgnoreCase(r));
    }
}
