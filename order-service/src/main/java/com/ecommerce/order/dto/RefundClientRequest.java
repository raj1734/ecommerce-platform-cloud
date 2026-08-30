package com.ecommerce.order.dto; import lombok.*; import java.math.BigDecimal;
@Data @Builder @NoArgsConstructor @AllArgsConstructor public class RefundClientRequest { private BigDecimal amount; private String reason; }
