package com.ecommerce.catalog.repository;

import com.ecommerce.catalog.entity.Product;

import java.util.List;

public interface ProductRepository {
    List<Product> findAll();
    Product findById(String id);
    Product save(Product product);
    void deleteById(String id);
    boolean existsById(String id);
    List<Product> findByStatus(String status);
    List<Product> findByCategoryAndStatus(String category, String status);
    List<Product> findByNameContainingIgnoreCase(String name);
    long count();
    void deleteAll();
}
