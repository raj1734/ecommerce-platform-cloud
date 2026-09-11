package com.ecommerce.order.dto; import lombok.*;
@Data @NoArgsConstructor @AllArgsConstructor public class RefundClientResponse { private String refundId; private String paymentId; private String status; }
