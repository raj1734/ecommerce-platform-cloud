package com.ecommerce.auth.repository;
import com.ecommerce.auth.entity.UserRole; import org.springframework.data.jpa.repository.JpaRepository; import java.util.List; import java.util.UUID;
public interface UserRoleRepository extends JpaRepository<UserRole, UserRole.UserRoleId> { List<UserRole> findAllByIdUserId(UUID userId); }
