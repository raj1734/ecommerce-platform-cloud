package com.ecommerce.order.client;

import com.ecommerce.order.dto.ProductResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "catalog-service", url = "${catalog.service.url:http://localhost:8082}")
public interface CatalogClient {
    @GetMapping("/api/v1/products/{id}")
    ProductResponse getProductById(@PathVariable("id") String id);
}
