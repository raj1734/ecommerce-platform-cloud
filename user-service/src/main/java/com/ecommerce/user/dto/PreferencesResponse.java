package com.ecommerce.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response payload for user preferences (LLD §11.5).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreferencesResponse {
    private String preferredLanguage;
    private String preferredCurrency;
    private Boolean marketingEnabled;
    private Boolean emailNotifications;
    private Boolean smsNotifications;
}
