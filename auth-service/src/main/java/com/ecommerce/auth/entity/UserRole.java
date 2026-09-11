package com.ecommerce.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.UUID;

@Entity @Table(name = "user_roles")
@Data @NoArgsConstructor @AllArgsConstructor
public class UserRole {
    @EmbeddedId private UserRoleId id;
    @Embeddable @Data @NoArgsConstructor @AllArgsConstructor
    public static class UserRoleId implements Serializable {
        @Column(name = "user_id") private UUID userId;
        @Column(name = "role_id") private UUID roleId;
    }
}
