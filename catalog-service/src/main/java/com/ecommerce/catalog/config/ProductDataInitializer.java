package com.ecommerce.catalog.config;

import com.ecommerce.catalog.entity.Product;
import com.ecommerce.catalog.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class ProductDataInitializer {

    private final ProductRepository repository;

    @Bean
    CommandLineRunner loadSampleProducts() {

        return args -> {

            Instant now = Instant.now();

            seedProduct(createLaptop(now));
            seedProduct(createSmartphone(now));
            seedProduct(createHeadphones(now));
            seedProduct(createSmartwatch(now));
            seedProduct(createTablet(now));
            seedProduct(createMonitor(now));
            seedProduct(createKeyboard(now));
            seedProduct(createMouse(now));

            System.out.println("=================================================");
            System.out.println(
                    "Catalog initialization completed. Total products: "
                            + repository.count()
            );
            System.out.println("=================================================");
        };
    }

    /**
     * Adds a product only when it does not already exist.
     *
     * This works for both:
     * 1. In-memory repository
     * 2. MongoDB repository
     */
    private void seedProduct(Product product) {

        if (repository.existsById(product.getId())) {

            System.out.println(
                    "Product already exists. Skipping: "
                            + product.getId()
                            + " - "
                            + product.getName()
            );

            return;
        }

        repository.save(product);

        System.out.println(
                "Created sample product: "
                        + product.getId()
                        + " - "
                        + product.getName()
        );
    }

    // -------------------------------------------------------------------------
    // LAPTOP
    // -------------------------------------------------------------------------

    private Product createLaptop(Instant now) {

        return Product.builder()
                .id("P1001")
                .sku("LAPTOP-001")
                .name("Dell Inspiron 15")
                .description(
                        "Dell Inspiron 15 business laptop with a 15.6-inch "
                                + "Full HD display, Intel processor and fast SSD storage."
                )
                .category("Laptops")
                .brand("Dell")
                .pricing(
                        Product.Pricing.builder()
                                .amount(new BigDecimal("74999.00"))
                                .currency("INR")
                                .build()
                )
                .attributes(
                        attributes(
                                "color", "Silver",
                                "processor", "Intel Core i5",
                                "ram", "16GB",
                                "storage", "512GB SSD",
                                "display", "15.6 inch Full HD",
                                "operatingSystem", "Windows 11"
                        )
                )
                .imageUrls(
                        List.of(
                                "https://placehold.co/800x800/png?text=Dell+Inspiron+15",
                                "https://placehold.co/800x800/png?text=Dell+Inspiron+15+Back"
                        )
                )
                .status("ACTIVE")
                .createdAt(now)
                .updatedAt(now)
                
                .build();
    }

    // -------------------------------------------------------------------------
    // SMARTPHONE
    // -------------------------------------------------------------------------

    private Product createSmartphone(Instant now) {

        return Product.builder()
                .id("P1002")
                .sku("PHONE-001")
                .name("Samsung Galaxy S25")
                .description(
                        "Premium Samsung smartphone with an AMOLED display, "
                                + "high-performance processor and advanced camera system."
                )
                .category("Mobiles")
                .brand("Samsung")
                .pricing(
                        Product.Pricing.builder()
                                .amount(new BigDecimal("79999.00"))
                                .currency("INR")
                                .build()
                )
                .attributes(
                        attributes(
                                "color", "Phantom Black",
                                "ram", "12GB",
                                "storage", "256GB",
                                "display", "6.2 inch AMOLED",
                                "camera", "50MP Triple Camera",
                                "battery", "4000mAh"
                        )
                )
                .imageUrls(
                        List.of(
                                "https://placehold.co/800x800/png?text=Samsung+Galaxy+S25",
                                "https://placehold.co/800x800/png?text=Galaxy+S25+Back"
                        )
                )
                .status("ACTIVE")
                .createdAt(now)
                .updatedAt(now)
                
                .build();
    }

    // -------------------------------------------------------------------------
    // HEADPHONES
    // -------------------------------------------------------------------------

    private Product createHeadphones(Instant now) {

        return Product.builder()
                .id("P1003")
                .sku("HEADPHONE-001")
                .name("Sony WH-1000XM6")
                .description(
                        "Premium wireless noise-cancelling headphones with "
                                + "high-resolution audio and long battery life."
                )
                .category("Audio")
                .brand("Sony")
                .pricing(
                        Product.Pricing.builder()
                                .amount(new BigDecimal("34999.00"))
                                .currency("INR")
                                .build()
                )
                .attributes(
                        attributes(
                                "color", "Black",
                                "type", "Over-Ear",
                                "connectivity", "Bluetooth 5.3",
                                "noiseCancellation", "Active Noise Cancellation",
                                "batteryLife", "30 hours",
                                "microphone", "Built-in"
                        )
                )
                .imageUrls(
                        List.of(
                                "https://placehold.co/800x800/png?text=Sony+WH-1000XM6",
                                "https://placehold.co/800x800/png?text=Sony+Headphones+Side"
                        )
                )
                .status("ACTIVE")
                .createdAt(now)
                .updatedAt(now)
                
                .build();
    }

    // -------------------------------------------------------------------------
    // SMARTWATCH
    // -------------------------------------------------------------------------

    private Product createSmartwatch(Instant now) {

        return Product.builder()
                .id("P1004")
                .sku("WATCH-001")
                .name("Apple Watch Series 10")
                .description(
                        "Advanced smartwatch with health monitoring, activity tracking, "
                                + "notifications and a bright edge-to-edge display."
                )
                .category("Wearables")
                .brand("Apple")
                .pricing(
                        Product.Pricing.builder()
                                .amount(new BigDecimal("46999.00"))
                                .currency("INR")
                                .build()
                )
                .attributes(
                        attributes(
                                "color", "Jet Black",
                                "size", "46mm",
                                "display", "OLED Retina",
                                "connectivity", "GPS + Cellular",
                                "waterResistance", "50m",
                                "batteryLife", "18 hours"
                        )
                )
                .imageUrls(
                        List.of(
                                "https://placehold.co/800x800/png?text=Apple+Watch+Series+10",
                                "https://placehold.co/800x800/png?text=Apple+Watch+Side"
                        )
                )
                .status("ACTIVE")
                .createdAt(now)
                .updatedAt(now)
                
                .build();
    }

    // -------------------------------------------------------------------------
    // TABLET
    // -------------------------------------------------------------------------

    private Product createTablet(Instant now) {

        return Product.builder()
                .id("P1005")
                .sku("TABLET-001")
                .name("iPad Air")
                .description(
                        "Lightweight tablet with a high-resolution Liquid Retina display "
                                + "and powerful Apple silicon performance."
                )
                .category("Tablets")
                .brand("Apple")
                .pricing(
                        Product.Pricing.builder()
                                .amount(new BigDecimal("59999.00"))
                                .currency("INR")
                                .build()
                )
                .attributes(
                        attributes(
                                "color", "Starlight",
                                "storage", "128GB",
                                "display", "11 inch Liquid Retina",
                                "processor", "Apple M-series",
                                "connectivity", "Wi-Fi"
                        )
                )
                .imageUrls(
                        List.of(
                                "https://placehold.co/800x800/png?text=iPad+Air",
                                "https://placehold.co/800x800/png?text=iPad+Air+Back"
                        )
                )
                .status("ACTIVE")
                .createdAt(now)
                .updatedAt(now)
                
                .build();
    }

    // -------------------------------------------------------------------------
    // MONITOR
    // -------------------------------------------------------------------------

    private Product createMonitor(Instant now) {

        return Product.builder()
                .id("P1006")
                .sku("MONITOR-001")
                .name("Dell UltraSharp 27")
                .description(
                        "27-inch professional monitor with sharp QHD resolution, "
                                + "wide viewing angles and excellent color accuracy."
                )
                .category("Monitors")
                .brand("Dell")
                .pricing(
                        Product.Pricing.builder()
                                .amount(new BigDecimal("42999.00"))
                                .currency("INR")
                                .build()
                )
                .attributes(
                        attributes(
                                "color", "Black",
                                "size", "27 inch",
                                "resolution", "2560x1440",
                                "refreshRate", "75Hz",
                                "panel", "IPS",
                                "connectivity", "HDMI + DisplayPort"
                        )
                )
                .imageUrls(
                        List.of(
                                "https://placehold.co/800x800/png?text=Dell+UltraSharp+27",
                                "https://placehold.co/800x800/png?text=Dell+Monitor+Back"
                        )
                )
                .status("ACTIVE")
                .createdAt(now)
                .updatedAt(now)
                
                .build();
    }

    // -------------------------------------------------------------------------
    // KEYBOARD
    // -------------------------------------------------------------------------

    private Product createKeyboard(Instant now) {

        return Product.builder()
                .id("P1007")
                .sku("KEYBOARD-001")
                .name("Logitech MX Mechanical")
                .description(
                        "Premium mechanical wireless keyboard designed for office "
                                + "productivity with comfortable tactile switches."
                )
                .category("Accessories")
                .brand("Logitech")
                .pricing(
                        Product.Pricing.builder()
                                .amount(new BigDecimal("11999.00"))
                                .currency("INR")
                                .build()
                )
                .attributes(
                        attributes(
                                "color", "Graphite",
                                "layout", "Full Size",
                                "switchType", "Tactile",
                                "connectivity", "Bluetooth + USB",
                                "backlight", "White LED",
                                "batteryLife", "Up to 15 days"
                        )
                )
                .imageUrls(
                        List.of(
                                "https://placehold.co/800x800/png?text=Logitech+MX+Mechanical",
                                "https://placehold.co/800x800/png?text=MX+Mechanical+Keyboard"
                        )
                )
                .status("ACTIVE")
                .createdAt(now)
                .updatedAt(now)
                
                .build();
    }

    // -------------------------------------------------------------------------
    // MOUSE
    // -------------------------------------------------------------------------

    private Product createMouse(Instant now) {

        return Product.builder()
                .id("P1008")
                .sku("MOUSE-001")
                .name("Logitech MX Master 3S")
                .description(
                        "Ergonomic wireless productivity mouse with precision tracking, "
                                + "silent clicks and multi-device connectivity."
                )
                .category("Accessories")
                .brand("Logitech")
                .pricing(
                        Product.Pricing.builder()
                                .amount(new BigDecimal("8999.00"))
                                .currency("INR")
                                .build()
                )
                .attributes(
                        attributes(
                                "color", "Graphite",
                                "sensor", "8000 DPI",
                                "connectivity", "Bluetooth + USB Receiver",
                                "buttons", "7",
                                "batteryLife", "70 days",
                                "silentClick", true
                        )
                )
                .imageUrls(
                        List.of(
                                "https://placehold.co/800x800/png?text=Logitech+MX+Master+3S",
                                "https://placehold.co/800x800/png?text=MX+Master+3S+Side"
                        )
                )
                .status("ACTIVE")
                .createdAt(now)
                .updatedAt(now)
                
                .build();
    }

    // -------------------------------------------------------------------------
    // ATTRIBUTE HELPER
    // -------------------------------------------------------------------------

    private Map<String, Object> attributes(Object... values) {

        Map<String, Object> attributes = new HashMap<>();

        for (int i = 0; i < values.length; i += 2) {
            attributes.put(
                    String.valueOf(values[i]),
                    values[i + 1]
            );
        }

        return attributes;
    }
}