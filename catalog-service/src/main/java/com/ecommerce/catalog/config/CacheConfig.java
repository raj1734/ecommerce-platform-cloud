package com.ecommerce.catalog.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    @ConditionalOnProperty(
            name = "catalog.cache.type",
            havingValue = "memory",
            matchIfMissing = true
    )
    public CacheManager memoryCacheManager() {
        return new ConcurrentMapCacheManager("products");
    }

    @Bean
    @ConditionalOnProperty(
            name = "catalog.cache.type",
            havingValue = "redis"
    )
    public CacheManager redisCacheManager(
            RedisConnectionFactory connectionFactory) {

        ObjectMapper redisObjectMapper = new ObjectMapper();

        // Support Instant, LocalDateTime, LocalDate, etc.
        redisObjectMapper.registerModule(new JavaTimeModule());

        // Store dates as ISO-8601 strings instead of timestamps.
        redisObjectMapper.disable(
                SerializationFeature.WRITE_DATES_AS_TIMESTAMPS
        );

        // IMPORTANT:
        // Include Java type information so Redis can reconstruct
        // Product instead of returning LinkedHashMap.
        redisObjectMapper.activateDefaultTyping(
                redisObjectMapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

        GenericJackson2JsonRedisSerializer redisSerializer =
                new GenericJackson2JsonRedisSerializer(redisObjectMapper);

        RedisCacheConfiguration config =
                RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(Duration.ofMinutes(10))
                        .serializeKeysWith(
                                RedisSerializationContext.SerializationPair
                                        .fromSerializer(
                                                new StringRedisSerializer()
                                        )
                        )
                        .serializeValuesWith(
                                RedisSerializationContext.SerializationPair
                                        .fromSerializer(redisSerializer)
                        );

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .build();
    }
}