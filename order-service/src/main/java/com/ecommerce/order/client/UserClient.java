package com.ecommerce.order.client;
import com.ecommerce.order.dto.AddressClientResponse; import org.springframework.cloud.openfeign.FeignClient; import org.springframework.web.bind.annotation.*; import java.util.List;
@FeignClient(name="user-service", url="${user.service.url:http://localhost:8087}")
public interface UserClient { @GetMapping("/api/v1/users/me/addresses") List<AddressClientResponse> getAddresses(@RequestHeader("X-User-Id") String userId); }
