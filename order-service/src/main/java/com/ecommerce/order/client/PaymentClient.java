package com.ecommerce.order.client;
import com.ecommerce.order.dto.*; import org.springframework.cloud.openfeign.FeignClient; import org.springframework.web.bind.annotation.*;
@FeignClient(name="payment-service", url="${payment.service.url:http://localhost:8088}")
public interface PaymentClient {
 @PostMapping("/api/v1/payments") PaymentClientResponse pay(@RequestBody PaymentClientRequest request,@RequestHeader("Idempotency-Key") String idempotencyKey);
 @PostMapping("/api/v1/payments/{paymentId}/refund") RefundClientResponse refund(@PathVariable String paymentId,@RequestBody RefundClientRequest request,@RequestHeader("Idempotency-Key") String idempotencyKey);
}
