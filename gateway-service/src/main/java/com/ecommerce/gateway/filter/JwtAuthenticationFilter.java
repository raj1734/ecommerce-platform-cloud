package com.ecommerce.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Override
    public Mono<Void> filter(
            ServerWebExchange exchange,
            org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {

        String path = exchange.getRequest()
                .getURI()
                .getPath();

        // Public endpoints
        if (path.startsWith("/api/v1/auth/")
                || path.startsWith("/actuator/")
        || path.startsWith("/api/v1/products")) {

            return chain.filter(exchange);
        }

        String authorizationHeader = exchange.getRequest()
                .getHeaders()
                .getFirst("Authorization");

        if (authorizationHeader == null
                || !authorizationHeader.startsWith("Bearer ")) {

            return unauthorized(exchange);
        }

        try {
            String token = authorizationHeader.substring(7);

            SecretKey key = Keys.hmacShaKeyFor(
                    jwtSecret.getBytes(StandardCharsets.UTF_8)
            );

            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String userId = claims.getSubject();

            String email = claims.get("email", String.class);

            Object rolesClaim = claims.get("roles");

            final String roles;

            if (rolesClaim instanceof List<?> roleList) {
                roles = String.join(
                        ",",
                        roleList.stream()
                                .map(Object::toString)
                                .toList()
                );
            } else {
                roles = "";
            }

            ServerHttpRequest request = exchange.getRequest()
                    .mutate()
                    .headers(headers -> {

                        // Remove any client-supplied identity headers.
                        headers.remove("X-User-Id");
                        headers.remove("X-User-Email");
                        headers.remove("X-User-Roles");

                        // Inject trusted identity information from JWT.
                        if (userId != null) {
                            headers.set("X-User-Id", userId);
                        }

                        if (email != null) {
                            headers.set("X-User-Email", email);
                        }

                        headers.set("X-User-Roles", roles);
                    })
                    .build();

            return chain.filter(
                    exchange.mutate()
                            .request(request)
                            .build()
            );

        } catch (Exception exception) {
            return unauthorized(exchange);
        }
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {

        exchange.getResponse()
                .setStatusCode(HttpStatus.UNAUTHORIZED);

        return exchange.getResponse()
                .setComplete();
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }
}