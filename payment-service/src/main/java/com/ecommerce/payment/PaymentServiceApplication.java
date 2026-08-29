package com.ecommerce.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Payment Service application entry point.
 *
 * <p>Currently a stub (LLD §7 &amp; §16): it exposes the payment/refund API contract
 * but has no database. Future external payment-provider integration is isolated
 * behind this service.</p>
 */
@SpringBootApplication
public class PaymentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentServiceApplication.class, args);
    }
}
