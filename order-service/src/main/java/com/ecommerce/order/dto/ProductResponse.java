package com.ecommerce.order.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductResponse {

    private String id;

    private String productId;

    private String sku;

    private String name;

    private String description;

    @JsonAlias({"pricing", "price"})
    private Price price;

    private String category;

    private String brand;

    private String status;

    @Data
    public static class Price {

        private BigDecimal amount;

        private String currency;
    }

    public String resolvedId() {
        return productId != null ? productId : id;
    }

    public BigDecimal resolvedPrice() {

        if (price == null || price.getAmount() == null) {
            throw new IllegalStateException(
                    "Catalog response does not contain product price"
            );
        }

        return price.getAmount();
    }
}