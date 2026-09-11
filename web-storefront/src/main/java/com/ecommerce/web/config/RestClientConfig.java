package com.ecommerce.web.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    RestClient gatewayRestClient(@Value("${gateway.url:http://localhost:8080}") String gatewayUrl) {
        return RestClient.builder()
                .baseUrl(gatewayUrl)
                .build();
    }
}
