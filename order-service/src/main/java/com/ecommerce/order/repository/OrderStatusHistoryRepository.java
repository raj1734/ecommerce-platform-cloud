package com.ecommerce.order.repository;

import java.util.UUID;
import com.ecommerce.order.entity.OrderStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for {@link OrderStatusHistory} (LLD §6.5).
 */
@Repository
public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, UUID> {
}
