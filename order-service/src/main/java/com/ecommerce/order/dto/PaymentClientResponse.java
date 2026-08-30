package com.ecommerce.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response from the Payment Service (LLD §16.1).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentClientResponse {
    private String paymentId;
    private String orderId;
    private String status;
}
