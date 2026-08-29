package com.ecommerce.order.config;

import com.ecommerce.order.event.OrderCreatedEvent;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.apache.kafka.clients.admin.NewTopic;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka Producer Configuration for the Order Service.
 *
 * <p>Aligned with the Low-Level Design (Section 2.4 - Order &amp; Cart Service and
 * the Assumptions in Section 1.7): the Order Service publishes the
 * {@code OrderCreated} event asynchronously to Apache Kafka (Amazon MSK).</p>
 *
 * <p>This configuration explicitly declares the producer factory, the typed
 * {@link KafkaTemplate}, and the {@code order-created} topic so the messaging
 * contract is self-documenting rather than relying purely on auto-configuration.</p>
 */
@Configuration
public class KafkaProducerConfig {

    public static final String ORDER_CREATED_TOPIC = "order-created";

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    /**
     * Builds the typed producer factory for {@link OrderCreatedEvent} messages.
     *
     * @return the configured {@link ProducerFactory}
     */
    @Bean
    public ProducerFactory<String, OrderCreatedEvent> producerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        config.put(ProducerConfig.ACKS_CONFIG, "all");
        return new DefaultKafkaProducerFactory<>(config);
    }

    /**
     * Provides the {@link KafkaTemplate} used by the Order Service to publish
     * order events.
     *
     * @return the configured {@link KafkaTemplate}
     */
    @Bean
    public KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    /**
     * Declares the {@code order-created} topic used for asynchronous order
     * notifications, as described in the LLD.
     *
     * @return the {@link NewTopic} definition
     */
    @Bean
    public NewTopic orderCreatedTopic() {
        return TopicBuilder.name(ORDER_CREATED_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
