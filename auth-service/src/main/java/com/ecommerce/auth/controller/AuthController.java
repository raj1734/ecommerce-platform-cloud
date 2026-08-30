package com.ecommerce.auth.controller;
import com.ecommerce.auth.dto.*; import com.ecommerce.auth.service.AuthService; import jakarta.validation.Valid; import lombok.RequiredArgsConstructor; import org.springframework.http.*; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/auth") @RequiredArgsConstructor
public class AuthController {
 private final AuthService authService;
 @PostMapping("/register") public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest r){return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(r));}
 @PostMapping("/login") public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest r){return ResponseEntity.ok(authService.login(r));}
 @PostMapping("/validate") public ResponseEntity<ValidateResponse> validate(@RequestHeader("Authorization") String h){return ResponseEntity.ok(authService.validate(h.replaceFirst("^Bearer ","")));}
 @PostMapping("/logout") public ResponseEntity<MessageResponse> logout(@RequestHeader("Authorization") String h){return ResponseEntity.ok(authService.logout(h.replaceFirst("^Bearer ","")));}
}
