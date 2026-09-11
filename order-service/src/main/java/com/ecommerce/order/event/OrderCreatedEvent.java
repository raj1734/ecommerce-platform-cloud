package com.ecommerce.order.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;
import java.time.LocalDateTime;

/**
 * OrderCreated Event for Kafka
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderCreatedEvent {
    private String eventId;
    private String eventType;
    private String eventVersion;
    private UUID orderId;
    private UUID userId;
    private String userEmail;
    private BigDecimal totalAmount;
    private String currency;
    private LocalDateTime occurredAt;
    private UUID correlationId;
}