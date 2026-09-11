package com.ecommerce.notification.listener;

import com.ecommerce.notification.config.KafkaConsumerConfig;
import com.ecommerce.notification.event.OrderCreatedEvent;
import com.ecommerce.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka listener for the {@code order-created} topic.
 *
 * <p>Implements the {@code notification-service-group} consumer described in
 * LLD Section 13.5 ("Send order confirmation emails/SMS").</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCreatedListener {

    private final NotificationService notificationService;

    @KafkaListener(
            topics = KafkaConsumerConfig.ORDER_CREATED_TOPIC,
            groupId = KafkaConsumerConfig.CONSUMER_GROUP,
            containerFactory = "kafkaListenerContainerFactory")
    public void onOrderCreated(OrderCreatedEvent event) {
        log.info("Received OrderCreatedEvent: orderId={}", event != null ? event.getOrderId() : null);
        notificationService.sendOrderConfirmation(event);
    }
}
