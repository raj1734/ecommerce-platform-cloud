package com.ecommerce.payment.service;

import com.ecommerce.payment.dto.PaymentRequest;
import com.ecommerce.payment.dto.PaymentResponse;
import com.ecommerce.payment.dto.RefundRequest;
import com.ecommerce.payment.dto.RefundResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stub payment processing (LLD §7 &amp; §16).
 *
 * <p>No persistence is used in the current scope. An in-memory idempotency
 * registry ensures that a retried {@code Idempotency-Key} returns the same result
 * instead of creating a duplicate payment/refund, matching the LLD idempotency
 * requirement (§9 / §16). Future work replaces this with a real provider and DB.</p>
 */
@Slf4j
@Service
public class PaymentService {

    private final ConcurrentHashMap<String, PaymentResponse> paymentIdempotency = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, RefundResponse> refundIdempotency = new ConcurrentHashMap<>();

    public PaymentResponse initiatePayment(PaymentRequest request, String idempotencyKey) {
        if (idempotencyKey != null && paymentIdempotency.containsKey(idempotencyKey)) {
            log.info("Idempotent replay for payment key={}", idempotencyKey);
            return paymentIdempotency.get(idempotencyKey);
        }
        PaymentResponse response = PaymentResponse.builder()
                .paymentId(UUID.randomUUID().toString())
                .orderId(request.getOrderId())
                .status("SUCCESS")
                .build();
        if (idempotencyKey != null) {
            paymentIdempotency.put(idempotencyKey, response);
        }
        log.info("Stub payment {} processed for orderId={} amount={}",
                response.getPaymentId(), request.getOrderId(), request.getAmount());
        return response;
    }

    public RefundResponse refundPayment(String paymentId, RefundRequest request, String idempotencyKey) {
        if (idempotencyKey != null && refundIdempotency.containsKey(idempotencyKey)) {
            log.info("Idempotent replay for refund key={}", idempotencyKey);
            return refundIdempotency.get(idempotencyKey);
        }
        RefundResponse response = RefundResponse.builder()
                .refundId(UUID.randomUUID().toString())
                .paymentId(paymentId)
                .status("REFUND_PENDING")
                .build();
        if (idempotencyKey != null) {
            refundIdempotency.put(idempotencyKey, response);
        }
        log.info("Stub refund {} initiated for paymentId={} amount={} reason={}",
                response.getRefundId(), paymentId, request.getAmount(), request.getReason());
        return response;
    }
}
