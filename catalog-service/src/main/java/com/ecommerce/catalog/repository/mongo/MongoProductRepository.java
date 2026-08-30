package com.ecommerce.catalog.repository.mongo;

import com.ecommerce.catalog.entity.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MongoProductRepository extends MongoRepository<Product, String> {
    List<Product> findByStatus(String status);
    List<Product> findByCategoryAndStatus(String category, String status);
    List<Product> findByNameContainingIgnoreCase(String name);
}
