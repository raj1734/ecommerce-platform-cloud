package com.ecommerce.inventory.event;
import lombok.*; import java.math.BigDecimal; import java.time.LocalDateTime; import java.util.UUID;
@Data @AllArgsConstructor @NoArgsConstructor
public class OrderCreatedEvent { private String eventId; private String eventType; private String eventVersion; private UUID orderId; private UUID userId; private String userEmail; private BigDecimal totalAmount; private String currency; private LocalDateTime occurredAt; private UUID correlationId; }
