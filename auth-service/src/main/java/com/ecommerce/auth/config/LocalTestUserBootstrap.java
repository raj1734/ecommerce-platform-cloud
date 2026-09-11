package com.ecommerce.auth.config;

import com.ecommerce.auth.entity.Role;
import com.ecommerce.auth.entity.User;
import com.ecommerce.auth.entity.UserRole;
import com.ecommerce.auth.repository.RoleRepository;
import com.ecommerce.auth.repository.UserRepository;
import com.ecommerce.auth.repository.UserRoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.UUID;

@Configuration
@Profile("local")
public class LocalTestUserBootstrap {

    public static final UUID RAJ_USER_ID =
            UUID.fromString("10000000-0000-0000-0000-000000000002");

    public static final UUID TEST_USER_ID =
            UUID.fromString("10000000-0000-0000-0000-000000000003");

    @Bean
    CommandLineRunner seedTestUsers(
            UserRepository users,
            RoleRepository roles,
            UserRoleRepository userRoles,
            PasswordEncoder encoder) {

        return args -> {

            Role userRole = roles.findByName("USER")
                    .orElseGet(() -> {

                        Role role = new Role();

                        role.setName("USER");
                        role.setDescription("Customer");

                        return roles.save(role);
                    });

            createUser(
                    users,
                    userRoles,
                    encoder,
                    RAJ_USER_ID,
                    "raj",
                    "raj@example.com",
                    "User@123",
                    userRole
            );

            createUser(
                    users,
                    userRoles,
                    encoder,
                    TEST_USER_ID,
                    "testuser",
                    "testuser@example.com",
                    "Test@123",
                    userRole
            );

            System.out.println("==============================================");
            System.out.println(" Local Test Users Created");
            System.out.println("==============================================");
            System.out.println("raj");
            System.out.println("  password : User@123");
            System.out.println("  userId   : " + RAJ_USER_ID);
            System.out.println();
            System.out.println("testuser");
            System.out.println("  password : Test@123");
            System.out.println("  userId   : " + TEST_USER_ID);
            System.out.println("==============================================");
        };
    }

    private void createUser(
            UserRepository users,
            UserRoleRepository userRoles,
            PasswordEncoder encoder,
            UUID userId,
            String username,
            String email,
            String password,
            Role role) {

        User user = users.findByUsername(username)
                .orElseGet(() -> {

                    User newUser = new User();

                    newUser.setId(userId);
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

        boolean roleAlreadyAssigned =
                userRoleRepositoryHasRole(
                        userRoles,
                        user.getId(),
                        role.getId()
                );

        if (!roleAlreadyAssigned) {

            userRoles.save(
                    new UserRole(
                            new UserRole.UserRoleId(
                                    user.getId(),
                                    role.getId()
                            )
                    )
            );
        }
    }

    private boolean userRoleRepositoryHasRole(
            UserRoleRepository userRoles,
            UUID userId,
            UUID roleId) {

        return userRoles.findAllByIdUserId(userId)
                .stream()
                .anyMatch(
                        userRole ->
                                userRole.getId()
                                        .getRoleId()
                                        .equals(roleId)
                );
    }
}