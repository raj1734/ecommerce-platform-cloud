package com.ecommerce.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response from the Inventory Service reservation call (LLD §13.2).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReserveInventoryResponse {
    private String reservationId;
    private String orderId;
    private String status;
}
