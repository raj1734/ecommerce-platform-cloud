package com.ecommerce.notification.service;

import com.ecommerce.notification.dto.NotificationRequest;
import com.ecommerce.notification.dto.NotificationResponse;
import com.ecommerce.notification.event.OrderCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Business logic for dispatching order confirmation notifications.
 *
 * <p>In a production deployment this would integrate with an email/SMS provider
 * (e.g., Amazon SES / SNS). For the current scope it logs the dispatch, which
 * keeps the service self-contained and testable locally.</p>
 */
@Slf4j
@Service
public class NotificationService {

    /**
     * Sends an order confirmation notification for the given event.
     *
     * @param event the consumed {@link OrderCreatedEvent}
     */
    public void sendOrderConfirmation(OrderCreatedEvent event) {
        if (event == null || event.getOrderId() == null) {
            log.warn("Received null or invalid OrderCreatedEvent; skipping notification");
            return;
        }
        log.info("Sending order confirmation to {} for orderId={} (amount={})",
                event.getUserEmail(), event.getOrderId(), event.getTotalAmount());
        // TODO: integrate with email/SMS provider (SES/SNS) per LLD Section 13.5
    }

    /**
     * Accepts an ad-hoc notification request (LLD §17.1). Stub implementation:
     * the request is acknowledged and logged; a real provider integration is
     * future work.
     *
     * @param request the notification request
     * @return an acknowledgement carrying a generated notification id
     */
    public NotificationResponse sendNotification(NotificationRequest request) {
        String notificationId = UUID.randomUUID().toString();
        log.info("Accepted notification {} type={} channel={} for userId={}",
                notificationId, request.getType(), request.getChannel(), request.getUserId());
        // TODO: integrate with email/SMS/push provider per LLD Section 17
        return NotificationResponse.builder()
                .notificationId(notificationId)
                .status("ACCEPTED")
                .build();
    }
}
