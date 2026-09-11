package com.ecommerce.gateway.config;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver; import org.springframework.context.annotation.Bean; import org.springframework.context.annotation.Configuration; import reactor.core.publisher.Mono;
@Configuration public class RateLimitConfig {
 @Bean public KeyResolver userKeyResolver(){return exchange -> {String user=exchange.getRequest().getHeaders().getFirst("X-User-Id"); if(user!=null&&!user.isBlank()) return Mono.just(user); var addr=exchange.getRequest().getRemoteAddress(); return Mono.just(addr==null?"anonymous":String.valueOf(addr.getAddress().getHostAddress()));};}
}
