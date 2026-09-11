package com.ecommerce.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Notification Service application entry point.
 *
 * <p>Consumes the {@code order-created} Kafka topic (consumer group
 * {@code notification-service-group}, LLD Section 13.5) and dispatches order
 * confirmation notifications (email/SMS).</p>
 */
@EnableKafka
@SpringBootApplication
public class NotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}
