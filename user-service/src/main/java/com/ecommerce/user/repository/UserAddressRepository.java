package com.ecommerce.user.repository;

import java.util.UUID;
import com.ecommerce.user.entity.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for {@link UserAddress} (LLD §3.2).
 */
@Repository
public interface UserAddressRepository extends JpaRepository<UserAddress, UUID> {
    List<UserAddress> findByUserId(UUID userId);
    void deleteByUserIdAndId(UUID userId, UUID id);
}
