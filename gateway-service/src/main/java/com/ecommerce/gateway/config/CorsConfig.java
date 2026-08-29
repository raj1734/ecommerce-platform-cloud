package com.ecommerce.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.reactive.CorsWebFilter;

import java.util.List;

/**
 * CORS Configuration for the API Gateway.
 *
 * <p>As per the Low-Level Design (Section 3.8), CORS is configured globally to
 * allow browser-based clients to securely access the backend APIs. This includes
 * allowed origins, HTTP methods, headers, and credentials support.</p>
 *
 * <p>Centralizing CORS at the Gateway avoids duplicating cross-origin
 * configuration across every downstream microservice.</p>
 */
@Configuration
public class CorsConfig {

    private static final String ALL = "*";

    /**
     * Builds a global {@link CorsWebFilter} applied to all routes handled by the
     * reactive Gateway.
     *
     * @return the configured {@link CorsWebFilter}
     */
    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOriginPatterns(List.of(ALL));
        corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        corsConfiguration.setAllowedHeaders(List.of(ALL));
        corsConfiguration.setExposedHeaders(List.of("Authorization", "X-Correlation-Id"));
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);

        return new CorsWebFilter(source);
    }
}
