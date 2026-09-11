package com.ecommerce.user.service;

import com.ecommerce.user.dto.*;
import com.ecommerce.user.entity.UserAddress;
import com.ecommerce.user.entity.UserPreference;
import com.ecommerce.user.entity.UserProfile;
import com.ecommerce.user.exception.ResourceNotFoundException;
import com.ecommerce.user.repository.UserAddressRepository;
import com.ecommerce.user.repository.UserPreferenceRepository;
import com.ecommerce.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Business logic for customer profiles, addresses and preferences (LLD §3 &amp; §11).
 *
 * <p>All operations are keyed by the authenticated user's identity ({@code authUserId})
 * propagated by the Gateway, so self-service endpoints never trust a client-provided id.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserProfileRepository profileRepository;
    private final UserAddressRepository addressRepository;
    private final UserPreferenceRepository preferenceRepository;

    @Transactional
    public UserProfileResponse getCurrentUser(UUID authUserId, String email) {
        UserProfile profile = profileRepository.findByAuthUserId(authUserId).orElseGet(() -> { UserProfile p = new UserProfile(); p.setAuthUserId(authUserId); p.setFirstName(""); p.setStatus(UserProfile.ProfileStatus.ACTIVE); return profileRepository.save(p); });
        return toProfileResponse(profile, email);
    }

    @Transactional
    public UserProfileResponse updateProfile(UUID authUserId, UpdateProfileRequest request) {
        UserProfile profile = profileRepository.findByAuthUserId(authUserId)
                .orElseGet(() -> {
                    UserProfile created = new UserProfile();
                    created.setAuthUserId(authUserId);
                    return created;
                });
        profile.setFirstName(request.getFirstName());
        profile.setLastName(request.getLastName());
        profile.setPhone(request.getPhone());
        UserProfile saved = profileRepository.save(profile);
        log.info("Updated profile for authUserId={}", authUserId);
        return toProfileResponse(saved, null);
    }

    @Transactional(readOnly = true)
    public List<AddressResponse> getAddresses(UUID authUserId) {
        UserProfile profile = requireProfile(authUserId);
        return addressRepository.findByUserId(profile.getId()).stream()
                .map(this::toAddressResponse)
                .toList();
    }

    @Transactional
    public AddressResponse addAddress(UUID authUserId, AddressRequest request) {
        UserProfile profile = requireProfile(authUserId);
        UserAddress address = new UserAddress();
        address.setUserId(profile.getId());
        address.setAddressLine1(request.getAddressLine1());
        address.setAddressLine2(request.getAddressLine2());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setPostalCode(request.getPostalCode());
        address.setCountry(request.getCountry());
        address.setIsDefault(Boolean.TRUE.equals(request.getIsDefault()));
        UserAddress saved = addressRepository.save(address);
        log.info("Added address {} for authUserId={}", saved.getId(), authUserId);
        return toAddressResponse(saved);
    }

    @Transactional
    public PreferencesResponse updatePreferences(UUID authUserId, PreferencesRequest request) {
        UserProfile profile = requireProfile(authUserId);
        UserPreference preference = preferenceRepository.findByUserId(profile.getId())
                .orElseGet(() -> {
                    UserPreference created = new UserPreference();
                    created.setUserId(profile.getId());
                    return created;
                });
        if (request.getPreferredLanguage() != null) {
            preference.setPreferredLanguage(request.getPreferredLanguage());
        }
        if (request.getPreferredCurrency() != null) {
            preference.setPreferredCurrency(request.getPreferredCurrency());
        }
        if (request.getMarketingEnabled() != null) {
            preference.setMarketingEnabled(request.getMarketingEnabled());
        }
        if (request.getEmailNotifications() != null) {
            preference.setEmailNotifications(request.getEmailNotifications());
        }
        if (request.getSmsNotifications() != null) {
            preference.setSmsNotifications(request.getSmsNotifications());
        }
        UserPreference saved = preferenceRepository.save(preference);
        return toPreferencesResponse(saved);
    }

    private UserProfile requireProfile(UUID authUserId) {
        return profileRepository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User profile not found for authUserId: " + authUserId));
    }

    private UserProfileResponse toProfileResponse(UserProfile profile, String email) {
        return UserProfileResponse.builder()
                .userId(profile.getAuthUserId().toString())
                .firstName(profile.getFirstName())
                .lastName(profile.getLastName())
                .email(email)
                .phone(profile.getPhone())
                .status(profile.getStatus() != null ? profile.getStatus().name() : null)
                .build();
    }

    private AddressResponse toAddressResponse(UserAddress address) {
        return AddressResponse.builder()
                .id(address.getId())
                .addressLine1(address.getAddressLine1())
                .addressLine2(address.getAddressLine2())
                .city(address.getCity())
                .state(address.getState())
                .postalCode(address.getPostalCode())
                .country(address.getCountry())
                .isDefault(address.getIsDefault())
                .build();
    }

    private PreferencesResponse toPreferencesResponse(UserPreference preference) {
        return PreferencesResponse.builder()
                .preferredLanguage(preference.getPreferredLanguage())
                .preferredCurrency(preference.getPreferredCurrency())
                .marketingEnabled(preference.getMarketingEnabled())
                .emailNotifications(preference.getEmailNotifications())
                .smsNotifications(preference.getSmsNotifications())
                .build();
    }
}
