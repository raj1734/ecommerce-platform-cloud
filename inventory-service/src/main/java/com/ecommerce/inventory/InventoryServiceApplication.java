package com.ecommerce.inventory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Inventory Service application entry point.
 *
 * <p>Consumes the {@code order-created} Kafka topic (consumer group
 * {@code inventory-service-group}, LLD Section 13.5) to update stock levels.</p>
 */
@EnableKafka
@SpringBootApplication
public class InventoryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(InventoryServiceApplication.class, args);
    }
}
