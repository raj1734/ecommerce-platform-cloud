package com.ecommerce.order.controller;

import java.util.UUID;
import com.ecommerce.order.dto.AddCartItemRequest;
import com.ecommerce.order.dto.CartResponse;
import com.ecommerce.order.dto.UpdateCartItemRequest;
import com.ecommerce.order.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Cart API contract (LLD §14). The authenticated user id is propagated by the
 * Gateway via the {@code X-User-Id} header after JWT validation.
 */
@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartResponse> getCart(@RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(cartService.getOrCreateCart(userId));
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItem(
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody AddCartItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cartService.addItem(userId, request));
    }

    @PatchMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> updateItem(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID itemId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        return ResponseEntity.ok(cartService.updateItem(userId, itemId, request));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> removeItem(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID itemId) {
        return ResponseEntity.ok(cartService.removeItem(userId, itemId));
    }
}
