package com.ecommerce.user.controller;

import com.ecommerce.user.dto.*;
import com.ecommerce.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * User self-service API (LLD §11).
 *
 * <p>Uses the {@code /me} convention so operations act on the authenticated user
 * identified by the {@code X-User-Id} header propagated from the Gateway after JWT
 * validation — a client-provided id is never trusted.</p>
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getCurrentUser(@RequestHeader("X-User-Id") UUID authUserId, @RequestHeader(value="X-User-Email", required=false) String email) {
        return ResponseEntity.ok(userService.getCurrentUser(authUserId, email));
    }

    @PutMapping("/me")
    public ResponseEntity<UserProfileResponse> updateProfile(
            @RequestHeader("X-User-Id") UUID authUserId,
            @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(userService.updateProfile(authUserId, request));
    }

    @GetMapping("/me/addresses")
    public ResponseEntity<List<AddressResponse>> getAddresses(@RequestHeader("X-User-Id") UUID authUserId) {
        return ResponseEntity.ok(userService.getAddresses(authUserId));
    }

    @PostMapping("/me/addresses")
    public ResponseEntity<AddressResponse> addAddress(
            @RequestHeader("X-User-Id") UUID authUserId,
            @Valid @RequestBody AddressRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.addAddress(authUserId, request));
    }

    @PutMapping("/me/preferences")
    public ResponseEntity<PreferencesResponse> updatePreferences(
            @RequestHeader("X-User-Id") UUID authUserId,
            @Valid @RequestBody PreferencesRequest request) {
        return ResponseEntity.ok(userService.updatePreferences(authUserId, request));
    }
}
