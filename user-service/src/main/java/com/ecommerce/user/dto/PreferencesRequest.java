package com.ecommerce.user.dto;

import lombok.Data;

/**
 * Request payload for updating user preferences (LLD §11.5).
 */
@Data
public class PreferencesRequest {
    private String preferredLanguage;
    private String preferredCurrency;
    private Boolean marketingEnabled;
    private Boolean emailNotifications;
    private Boolean smsNotifications;
}
