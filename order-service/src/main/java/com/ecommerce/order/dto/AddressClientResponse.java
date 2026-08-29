package com.ecommerce.order.dto; import lombok.*; import java.util.UUID;
@Data @NoArgsConstructor @AllArgsConstructor public class AddressClientResponse { private UUID id; private String addressLine1; private String addressLine2; private String city; private String state; private String postalCode; private String country; private Boolean isDefault; }
