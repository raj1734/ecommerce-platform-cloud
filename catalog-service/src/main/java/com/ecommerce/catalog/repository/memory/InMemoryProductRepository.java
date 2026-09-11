package com.ecommerce.catalog.repository.memory;

import com.ecommerce.catalog.entity.Product;
import com.ecommerce.catalog.repository.ProductRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@ConditionalOnProperty(name = "catalog.repository.type", havingValue = "memory", matchIfMissing = true)
public class InMemoryProductRepository implements ProductRepository {

    private final Map<String, Product> products = new ConcurrentHashMap<>();

    @Override
    public List<Product> findAll() {
        return new ArrayList<>(products.values());
    }

    @Override
    public Product findById(String id) {
        return products.get(id);
    }

    @Override
    public Product save(Product product) {
        if (product.getId() == null || product.getId().isBlank()) {
            product.setId(UUID.randomUUID().toString());
        }
        products.put(product.getId(), product);
        return product;
    }

    @Override
    public void deleteById(String id) {
        products.remove(id);
    }

    @Override
    public boolean existsById(String id) {
        return products.containsKey(id);
    }

    @Override
    public List<Product> findByStatus(String status) {
        return products.values().stream()
                .filter(p -> status == null || status.equalsIgnoreCase(p.getStatus()))
                .toList();
    }

    @Override
    public List<Product> findByCategoryAndStatus(String category, String status) {
        return products.values().stream()
                .filter(p -> (category == null || category.equalsIgnoreCase(p.getCategory()))
                        && (status == null || status.equalsIgnoreCase(p.getStatus())))
                .toList();
    }

    @Override
    public List<Product> findByNameContainingIgnoreCase(String name) {
        if (name == null || name.isBlank()) {
            return findAll();
        }
        String search = name.toLowerCase();
        return products.values().stream()
                .filter(p -> p.getName() != null && p.getName().toLowerCase().contains(search))
                .toList();
    }

    @Override
    public long count() {
        return products.size();
    }

    @Override
    public void deleteAll() {
        products.clear();
    }
}
