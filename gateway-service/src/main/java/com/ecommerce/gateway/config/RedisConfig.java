package com.ecommerce.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis Configuration for the API Gateway.
 *
 * <p>As per the Low-Level Design (Section 3.9), Redis is configured within the
 * Gateway to support distributed caching and future enhancements such as API
 * rate limiting, session storage, and distributed request counters.</p>
 *
 * <p>This configuration provides a reactive Redis connection factory and a
 * reactive Redis template that can be reused by rate-limiting filters and other
 * cross-cutting concerns in the reactive Gateway stack.</p>
 */
@Configuration
public class RedisConfig {

    @Value("${spring.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.redis.port:6379}")
    private int redisPort;

    @Value("${spring.redis.password:}")
    private String redisPassword;

    @Value("${spring.redis.ssl.enabled:false}")
    private boolean redisSsl;

    /**
     * Creates a reactive Lettuce-based Redis connection factory used by the
     * Gateway for distributed caching and future rate limiting.
     *
     * @return the configured {@link LettuceConnectionFactory}
     */
    @Bean
    public LettuceConnectionFactory reactiveRedisConnectionFactory() {
        RedisStandaloneConfiguration standaloneConfiguration =
                new RedisStandaloneConfiguration(redisHost, redisPort);
        if (redisPassword != null && !redisPassword.isBlank()) {
            standaloneConfiguration.setPassword(redisPassword);
        }
        LettuceClientConfiguration.LettuceClientConfigurationBuilder builder =
                LettuceClientConfiguration.builder();
        if (redisSsl) {
            builder.useSsl();
        }
        return new LettuceConnectionFactory(standaloneConfiguration, builder.build());
    }

    /**
     * Provides a reactive Redis template with String key/value serialization.
     * This template is the entry point for distributed request counters and
     * rate-limiting logic described in the LLD.
     *
     * @param connectionFactory the reactive Redis connection factory
     * @return the configured {@link ReactiveRedisTemplate}
     */
    @Bean
    public ReactiveRedisTemplate<String, String> reactiveRedisTemplate(
            LettuceConnectionFactory connectionFactory) {
        RedisSerializationContext<String, String> serializationContext =
                RedisSerializationContext
                        .<String, String>newSerializationContext(new StringRedisSerializer())
                        .value(new StringRedisSerializer())
                        .build();
        return new ReactiveRedisTemplate<>(connectionFactory, serializationContext);
    }
}
