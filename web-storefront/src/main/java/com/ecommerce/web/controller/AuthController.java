package com.ecommerce.web.controller;

import com.ecommerce.web.model.SessionUser;
import com.ecommerce.web.service.GatewayClient;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;

@Controller
public class AuthController {
    private final GatewayClient gateway;

    public AuthController(GatewayClient gateway) {
        this.gateway = gateway;
    }

    @GetMapping({"/", "/login"})
    public String loginPage(HttpSession session, Model model) {
        if (gateway.authenticated(session)) return "redirect:/products";
        return "auth/login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session,
                        Model model) {
        JsonNode response = gateway.post("/api/v1/auth/login",
                java.util.Map.of("username", username, "password", password), session);

        String token = first(response, "token", "accessToken", "access_token", "jwt");
        if (token.isBlank()) {
            model.addAttribute("error", gateway.text(response, "message").isBlank()
                    ? gateway.text(response, "error") : gateway.text(response, "message"));
            if (model.getAttribute("error") == null || model.getAttribute("error").toString().isBlank())
                model.addAttribute("error", "Login failed");
            return "auth/login";
        }

        JsonNode jwtClaims = decodeJwtClaims(token);

        String userId = first(response, "userId", "id", "subject");
        if (userId.isBlank()) userId = first(jwtClaims, "sub", "userId", "user_id", "id");

        String email = first(response, "email", "userEmail");
        if (email.isBlank()) email = first(jwtClaims, "email", "userEmail");

        String returnedUsername = first(response, "username", "user");
        if (returnedUsername.isBlank()) returnedUsername = first(jwtClaims, "username", "preferred_username", "user");

        List<String> roles = extractRoles(response);
        if (roles.isEmpty()) roles = extractRoles(jwtClaims);

        session.setAttribute(GatewayClient.AUTH_USER,
                new SessionUser(userId,
                        returnedUsername.isBlank() ? username : returnedUsername,
                        email, token, roles));
        session.setAttribute(GatewayClient.CORRELATION_ID, java.util.UUID.randomUUID().toString());

        return "redirect:/products";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String email,
                           @RequestParam String password,
                           HttpSession session,
                           Model model) {
        JsonNode response = gateway.post("/api/v1/auth/register",
                java.util.Map.of("username", username, "email", email, "password", password), session);

        if (response.has("error") || (response.has("status") && response.get("status").asInt() >= 400)) {
            model.addAttribute("error", gateway.text(response, "message").isBlank()
                    ? gateway.text(response, "error") : gateway.text(response, "message"));
            return "auth/register";
        }
        model.addAttribute("success", "Registration successful. Please sign in.");
        return "auth/login";
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        if (gateway.authenticated(session)) gateway.post("/api/v1/auth/logout", null, session);
        session.invalidate();
        return "redirect:/login?logout";
    }


    private List<String> extractRoles(JsonNode node) {
        List<String> roles = new ArrayList<>();
        if (node == null || node.isMissingNode() || node.isNull()) return roles;

        for (String field : List.of("roles", "authorities", "role", "authority")) {
            JsonNode value = node.get(field);
            if (value == null || value.isNull()) continue;

            if (value.isArray()) {
                for (JsonNode item : value) addRoleValue(roles, item);
            } else {
                addRoleValue(roles, value);
            }
        }

        // Some JWTs use a space-separated scope claim.
        JsonNode scope = node.get("scope");
        if (scope != null && scope.isTextual()) {
            for (String value : scope.asText().split("\\s+")) {
                addRoleValue(roles, mapperRoleValue(value));
            }
        }

        return roles.stream().distinct().toList();
    }

    private void addRoleValue(List<String> roles, JsonNode value) {
        if (value == null || value.isNull()) return;
        if (value.isTextual()) {
            String role = value.asText().trim();
            if (!role.isBlank()) roles.add(role);
            return;
        }
        if (value.isObject()) {
            for (String field : List.of("name", "role", "authority")) {
                JsonNode nested = value.get(field);
                if (nested != null && nested.isTextual() && !nested.asText().isBlank()) {
                    roles.add(nested.asText().trim());
                    return;
                }
            }
        }
    }

    private JsonNode mapperRoleValue(String value) {
        return new com.fasterxml.jackson.databind.node.TextNode(value);
    }

    private JsonNode decodeJwtClaims(String token) {
        if (token == null || token.isBlank()) return com.fasterxml.jackson.databind.node.MissingNode.getInstance();
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) return com.fasterxml.jackson.databind.node.MissingNode.getInstance();
            byte[] decoded = Base64.getUrlDecoder().decode(parts[1]);
            return new com.fasterxml.jackson.databind.ObjectMapper().readTree(decoded);
        } catch (Exception ignored) {
            return com.fasterxml.jackson.databind.node.MissingNode.getInstance();
        }
    }

    private String first(JsonNode n, String... fields) {
        for (String f : fields) {
            String v = gateway.text(n, f);
            if (!v.isBlank()) return v;
        }
        return "";
    }
}
