package com.ecommerce.catalog.dto;
import jakarta.validation.Valid; import jakarta.validation.constraints.*; import lombok.Data; import java.math.BigDecimal; import java.util.*;
@Data public class ProductRequest {
 @NotBlank private String sku; @NotBlank private String name; private String description; @NotBlank private String category; private String brand;
 @NotNull @Valid private PriceRequest price; private Map<String,Object> attributes; private List<String> imageUrls;
 @Data public static class PriceRequest { @NotNull @DecimalMin("0.0") private BigDecimal amount; @NotBlank @Size(min=3,max=3) private String currency; }
}
