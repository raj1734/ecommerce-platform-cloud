package com.ecommerce.inventory.listener;

import com.ecommerce.inventory.config.KafkaConsumerConfig;
import com.ecommerce.inventory.event.OrderCreatedEvent;
import com.ecommerce.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka listener for the {@code order-created} topic.
 *
 * <p>Implements the {@code inventory-service-group} consumer described in
 * LLD Section 13.5 ("Update stock levels").</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCreatedListener {

    private final InventoryService inventoryService;

    @KafkaListener(
            topics = KafkaConsumerConfig.ORDER_CREATED_TOPIC,
            groupId = KafkaConsumerConfig.CONSUMER_GROUP,
            containerFactory = "kafkaListenerContainerFactory")
    public void onOrderCreated(OrderCreatedEvent event) {
        log.info("Received OrderCreatedEvent: orderId={}", event != null ? event.getOrderId() : null);
        inventoryService.updateStockForOrder(event);
    }
}
