package com.ecommerce.catalog.repository.mongo;

import com.ecommerce.catalog.entity.Product;
import com.ecommerce.catalog.repository.ProductRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@ConditionalOnProperty(name = "catalog.repository.type", havingValue = "mongo")
public class MongoProductRepositoryAdapter implements ProductRepository {

    private final MongoProductRepository delegate;

    public MongoProductRepositoryAdapter(MongoProductRepository delegate) {
        this.delegate = delegate;
    }

    @Override public List<Product> findAll() { return delegate.findAll(); }
    @Override public Product findById(String id) { return delegate.findById(id).orElse(null); }
    @Override public Product save(Product product) { return delegate.save(product); }
    @Override public void deleteById(String id) { delegate.deleteById(id); }
    @Override public boolean existsById(String id) { return delegate.existsById(id); }
    @Override public List<Product> findByStatus(String status) { return delegate.findByStatus(status); }
    @Override public List<Product> findByCategoryAndStatus(String category, String status) { return delegate.findByCategoryAndStatus(category, status); }
    @Override public List<Product> findByNameContainingIgnoreCase(String name) { return delegate.findByNameContainingIgnoreCase(name); }
    @Override public long count() { return delegate.count(); }
    @Override public void deleteAll() { delegate.deleteAll(); }
}
