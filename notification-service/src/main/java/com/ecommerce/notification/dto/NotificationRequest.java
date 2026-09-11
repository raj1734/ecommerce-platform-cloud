package com.ecommerce.notification.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Send notification request (LLD §17.1).
 */
@Data
public class NotificationRequest {
    @NotBlank(message = "userId is required")
    private String userId;

    @NotBlank(message = "type is required")
    private String type;

    @NotBlank(message = "channel is required")
    private String channel;

    @NotBlank(message = "message is required")
    private String message;
}
