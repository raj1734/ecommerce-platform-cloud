package com.ecommerce.catalog.controller;

import com.ecommerce.catalog.dto.ProductRequest;
import com.ecommerce.catalog.dto.ProductPageResponse;
import com.ecommerce.catalog.entity.Product;
import com.ecommerce.catalog.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService service;

    @GetMapping
    public ResponseEntity<ProductPageResponse> getAll(@RequestParam(required = false) String category, @RequestParam(required = false) String brand, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        if (size < 1) size = 20;
        List<Product> all = category != null ? service.getProductsByCategory(category) : service.getAllProducts();
        if (brand != null) all = all.stream().filter(p -> brand.equalsIgnoreCase(p.getBrand())).toList();
        int from = Math.min(page * size, all.size());
        int to = Math.min(from + size, all.size());
        int totalPages = (int) Math.ceil(all.size() / (double) size);
        return ResponseEntity.ok(ProductPageResponse.builder().content(all.subList(from, to)).page(page).size(size).totalElements(all.size()).totalPages(totalPages).build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> get(@PathVariable String id) {
        return ResponseEntity.ok(service.getProductById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Product>> search(@RequestParam String name) {
        return ResponseEntity.ok(service.searchProducts(name));
    }

    @PostMapping
    public ResponseEntity<Product> create(@RequestHeader(value = "X-User-Roles", required = false) String roles, @Valid @RequestBody ProductRequest r) {
        requireAdmin(roles);
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createProduct(r));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> update(@RequestHeader(value = "X-User-Roles", required = false) String roles, @PathVariable String id, @Valid @RequestBody ProductRequest r) {
        requireAdmin(roles);
        return ResponseEntity.ok(service.updateProduct(id, r));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Product> status(@RequestHeader(value = "X-User-Roles", required = false) String roles, @PathVariable String id, @RequestBody Map<String, String> body) {
        requireAdmin(roles);
        return ResponseEntity.ok(service.updateStatus(id, body.getOrDefault("status", "INACTIVE")));
    }

    private void requireAdmin(String roles) {
        if (roles == null || !List.of(roles.split(",")).contains("ADMIN"))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "ADMIN role required");
    }
}
