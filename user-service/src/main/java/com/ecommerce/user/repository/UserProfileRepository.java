package com.ecommerce.user.repository;

import java.util.UUID;
import com.ecommerce.user.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for {@link UserProfile} (LLD §3.1).
 */
@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {
    Optional<UserProfile> findByAuthUserId(UUID authUserId);
    boolean existsByAuthUserId(UUID authUserId);
}
