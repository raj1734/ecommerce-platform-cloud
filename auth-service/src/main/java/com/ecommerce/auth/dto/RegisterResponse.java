package com.ecommerce.auth.dto;
import lombok.*;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class RegisterResponse { private String userId; private String username; private String message; }
