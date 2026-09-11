package com.ecommerce.catalog.dto;
import com.ecommerce.catalog.entity.Product; import lombok.*; import java.util.List;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ProductPageResponse { private List<Product> content; private int page; private int size; private long totalElements; private int totalPages; }
