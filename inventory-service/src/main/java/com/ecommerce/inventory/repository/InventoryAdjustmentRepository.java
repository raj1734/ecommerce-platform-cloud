package com.ecommerce.inventory.repository;

import com.ecommerce.inventory.entity.InventoryAdjustment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InventoryAdjustmentRepository extends JpaRepository<InventoryAdjustment, UUID> {
    List<InventoryAdjustment> findTop50ByProductIdOrderByCreatedAtDesc(String productId);
}
