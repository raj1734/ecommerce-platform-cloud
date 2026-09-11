package com.ecommerce.catalog.service;

import com.ecommerce.catalog.dto.ProductRequest;
import com.ecommerce.catalog.entity.Product;
import com.ecommerce.catalog.exception.ProductNotFoundException;
import com.ecommerce.catalog.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository repo;

    @Cacheable(value = "products", key = "'all'")
    public List<Product> getAllProducts() {
        return repo.findByStatus("ACTIVE");
    }

    @Cacheable(value = "products", key = "#id")
    public Product getProductById(String id) {
        Product product = repo.findById(id);
        if (product == null) {
            throw new ProductNotFoundException("Product not found with id: " + id);
        }
        return product;
    }

    @Cacheable(value = "products", key = "'category:'+#category")
    public List<Product> getProductsByCategory(String category) {
        return repo.findByCategoryAndStatus(category, "ACTIVE");
    }

    public List<Product> searchProducts(String name) {
        return repo.findByNameContainingIgnoreCase(name)
                .stream()
                .filter(p -> "ACTIVE".equalsIgnoreCase(p.getStatus()))
                .toList();
    }

    @CacheEvict(value = "products", allEntries = true)
    public Product createProduct(ProductRequest r) {
        Product p = new Product();
        apply(p, r);
        p.setStatus("ACTIVE");
        return repo.save(p);
    }

    @CacheEvict(value = "products", allEntries = true)
    public Product updateProduct(String id, ProductRequest r) {
        Product p = getProductById(id);
        apply(p, r);
        return repo.save(p);
    }

    @CacheEvict(value = "products", allEntries = true)
    public Product updateStatus(String id, String status) {
        Product p = getProductById(id);
        p.setStatus(status);
        return repo.save(p);
    }

    private void apply(Product p, ProductRequest r) {
        p.setSku(r.getSku());
        p.setName(r.getName());
        p.setDescription(r.getDescription());
        p.setCategory(r.getCategory());
        p.setBrand(r.getBrand());
        p.setAttributes(r.getAttributes() == null ? new HashMap<>() : r.getAttributes());
        p.setImageUrls(r.getImageUrls() == null ? new ArrayList<>() : r.getImageUrls());
        p.setPricing(Product.Pricing.builder()
                .amount(r.getPrice().getAmount())
                .currency(r.getPrice().getCurrency())
                .build());
        if (p.getCreatedAt() == null) p.setCreatedAt(Instant.now());
        p.setUpdatedAt(Instant.now());
    }
}
