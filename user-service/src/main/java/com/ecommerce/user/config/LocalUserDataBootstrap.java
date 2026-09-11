package com.ecommerce.user.config;

import com.ecommerce.user.entity.UserAddress;
import com.ecommerce.user.entity.UserPreference;
import com.ecommerce.user.entity.UserProfile;
import com.ecommerce.user.repository.UserAddressRepository;
import com.ecommerce.user.repository.UserPreferenceRepository;
import com.ecommerce.user.repository.UserProfileRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.LocalDate;
import java.util.UUID;

@Configuration
@Profile("local")
public class LocalUserDataBootstrap {

    private static final UUID ADMIN_USER_ID =
            UUID.fromString("10000000-0000-0000-0000-000000000001");

    private static final UUID RAJ_USER_ID =
            UUID.fromString("10000000-0000-0000-0000-000000000002");

    private static final UUID TEST_USER_ID =
            UUID.fromString("10000000-0000-0000-0000-000000000003");

    @Bean
    CommandLineRunner seedUserData(
            UserProfileRepository profiles,
            UserAddressRepository addresses,
            UserPreferenceRepository preferences) {

        return args -> {

            UserProfile admin = createProfile(
                    profiles,
                    ADMIN_USER_ID,
                    "Admin",
                    "User",
                    "+919900000001",
                    LocalDate.of(1990, 1, 10)
            );

            UserProfile raj = createProfile(
                    profiles,
                    RAJ_USER_ID,
                    "Raj",
                    "Customer",
                    "+919900000002",
                    LocalDate.of(1995, 5, 15)
            );

            UserProfile testUser = createProfile(
                    profiles,
                    TEST_USER_ID,
                    "Test",
                    "User",
                    "+919900000003",
                    LocalDate.of(1998, 8, 20)
            );

            createAddress(
                    addresses,
                    raj.getId(),
                    "12 Main Street",
                    "Near City Mall",
                    "Hyderabad",
                    "Telangana",
                    "500001",
                    "India"
            );

            createAddress(
                    addresses,
                    testUser.getId(),
                    "45 Test Road",
                    "Apartment 202",
                    "Bengaluru",
                    "Karnataka",
                    "560001",
                    "India"
            );

            createAddress(
                    addresses,
                    admin.getId(),
                    "1 Admin Avenue",
                    "Corporate Office",
                    "Hyderabad",
                    "Telangana",
                    "500032",
                    "India"
            );

            createPreference(
                    preferences,
                    admin.getId()
            );

            createPreference(
                    preferences,
                    raj.getId()
            );

            createPreference(
                    preferences,
                    testUser.getId()
            );

            System.out.println("==============================================");
            System.out.println(" Local User Data Bootstrap completed");
            System.out.println("==============================================");
            System.out.println("Profiles    : 3");
            System.out.println("Addresses   : 3");
            System.out.println("Preferences : 3");
            System.out.println("==============================================");
        };
    }

    private UserProfile createProfile(
            UserProfileRepository repository,
            UUID authUserId,
            String firstName,
            String lastName,
            String phone,
            LocalDate dateOfBirth) {

        return repository.findByAuthUserId(authUserId)
                .orElseGet(() -> {

                    UserProfile profile = new UserProfile();

                    profile.setAuthUserId(authUserId);
                    profile.setFirstName(firstName);
                    profile.setLastName(lastName);
                    profile.setPhone(phone);
                    profile.setDateOfBirth(dateOfBirth);
                    profile.setStatus(
                            UserProfile.ProfileStatus.ACTIVE
                    );

                    return repository.save(profile);
                });
    }

    private void createAddress(
            UserAddressRepository repository,
            UUID userId,
            String line1,
            String line2,
            String city,
            String state,
            String postalCode,
            String country) {

        if (!repository.findByUserId(userId).isEmpty()) {
            return;
        }

        UserAddress address = new UserAddress();

        address.setUserId(userId);
        address.setAddressType("SHIPPING");
        address.setAddressLine1(line1);
        address.setAddressLine2(line2);
        address.setCity(city);
        address.setState(state);
        address.setPostalCode(postalCode);
        address.setCountry(country);
        address.setIsDefault(true);

        repository.save(address);
    }

    private void createPreference(
            UserPreferenceRepository repository,
            UUID userId) {

        if (repository.findByUserId(userId).isPresent()) {
            return;
        }

        UserPreference preference = new UserPreference();

        preference.setUserId(userId);
        preference.setPreferredLanguage("en");
        preference.setPreferredCurrency("INR");
        preference.setMarketingEnabled(true);
        preference.setEmailNotifications(true);
        preference.setSmsNotifications(false);

        repository.save(preference);
    }
}