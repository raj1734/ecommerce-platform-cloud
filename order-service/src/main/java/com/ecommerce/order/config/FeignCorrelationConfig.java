package com.ecommerce.order.config;
import feign.RequestInterceptor; import org.slf4j.MDC; import org.springframework.context.annotation.Bean; import org.springframework.context.annotation.Configuration;
@Configuration public class FeignCorrelationConfig { @Bean public RequestInterceptor correlationInterceptor(){ return template -> { String id=MDC.get("correlationId"); if(id!=null) template.header("X-Correlation-ID",id); }; } }
