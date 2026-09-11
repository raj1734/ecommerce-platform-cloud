package com.ecommerce.inventory.dto;
import lombok.*; import java.util.List;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ReservationResponse { private String reservationId; private String orderId; private String status; private List<String> reservationIds; }
