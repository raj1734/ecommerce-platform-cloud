package com.ecommerce.catalog.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "products")
@CompoundIndexes({
        @CompoundIndex(name = "idx_category_status", def = "{category:1,status:1}"),
        @CompoundIndex(name = "idx_brand", def = "{brand:1}"),
        @CompoundIndex(name = "idx_status", def = "{status:1}"),
        @CompoundIndex(name = "idx_name", def = "{name:1}")
})
public class Product {
    @Id
    private String id;
    @Indexed(unique = true)
    private String sku;
    private String name;
    private String description;
    private String category;
    private String brand;
    @JsonProperty("price")
    private Pricing pricing;
    @Builder.Default private Map<String, Object> attributes = new HashMap<>();
    @Builder.Default private List<String> imageUrls = new ArrayList<>();
    @Builder.Default private String status = "ACTIVE";
    @CreatedDate private Instant createdAt;
    @LastModifiedDate private Instant updatedAt;
    @Version private Long version;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Pricing {
        private BigDecimal amount;
        private String currency;
    }
}
