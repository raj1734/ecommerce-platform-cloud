package com.ecommerce.catalog.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@Configuration
@ConditionalOnProperty(name = "catalog.repository.type", havingValue = "mongo")
@EnableMongoAuditing
public class MongoConfig {
}
