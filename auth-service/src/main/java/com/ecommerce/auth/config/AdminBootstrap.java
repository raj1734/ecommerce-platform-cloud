package com.ecommerce.auth.config;

import com.ecommerce.auth.entity.Role;
import com.ecommerce.auth.entity.User;
import com.ecommerce.auth.entity.UserRole;
import com.ecommerce.auth.repository.RoleRepository;
import com.ecommerce.auth.repository.UserRepository;
import com.ecommerce.auth.repository.UserRoleRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.UUID;

@Configuration
public class AdminBootstrap {

    /*
     * Fixed ID so User Service can create the matching
     * profile/address records.
     */
    public static final UUID ADMIN_USER_ID =
            UUID.fromString("10000000-0000-0000-0000-000000000001");

    @Bean
    CommandLineRunner seedAdmin(
            UserRepository users,
            RoleRepository roles,
            UserRoleRepository userRoles,
            PasswordEncoder encoder,
            @Value("${auth.bootstrap-admin.enabled:false}") boolean enabled,
            @Value("${auth.bootstrap-admin.username:admin}") String username,
            @Value("${auth.bootstrap-admin.email:admin@example.com}") String email,
            @Value("${auth.bootstrap-admin.password:ChangeMe@123}") String password) {

        return args -> {

            if (!enabled) {
                return;
            }

            Role role = roles.findByName("ADMIN")
                    .orElseGet(() -> {
                        Role r = new Role();
                        r.setName("ADMIN");
                        r.setDescription("Administrator");
                        return roles.save(r);
                    });

            User user = users.findByUsername(username)
                    .orElseGet(() -> {

                        User newUser = new User();

                        newUser.setId(ADMIN_USER_ID);
                        newUser.setUsername(username);
                        newUser.setEmail(email);
                        newUser.setPasswordHash(
                                encoder.encode(password)
                        );
                        newUser.setStatus("ACTIVE");
                        newUser.setFailedLoginAttempts(0);
                        newUser.setPasswordChangedAt(
                                LocalDateTime.now()
                        );

                        return users.save(newUser);
                    });

            boolean alreadyAssigned =
                    userRoles.findAllByIdUserId(user.getId())
                            .stream()
                            .anyMatch(userRole ->
                                    userRole.getId()
                                            .getRoleId()
                                            .equals(role.getId())
                            );

            if (!alreadyAssigned) {

                userRoles.save(
                        new UserRole(
                                new UserRole.UserRoleId(
                                        user.getId(),
                                        role.getId()
                                )
                        )
                );
            }

            System.out.println("==============================================");
            System.out.println(" Admin Bootstrap completed");
            System.out.println("==============================================");
            System.out.println("Username : " + username);
            System.out.println("Email    : " + email);
            System.out.println("User ID  : " + user.getId());
            System.out.println("Role     : ADMIN");
            System.out.println("==============================================");
        };
    }
}