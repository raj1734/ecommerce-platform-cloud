package com.ecommerce.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request body sent to the Payment Service (LLD §16.1).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentClientRequest {
    private String orderId;
    private BigDecimal amount;
    private String currency;
    private String paymentMethod;
}
