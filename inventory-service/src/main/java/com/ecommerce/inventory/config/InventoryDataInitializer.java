package com.ecommerce.inventory.config;

import com.ecommerce.inventory.entity.Inventory;
import com.ecommerce.inventory.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class InventoryDataInitializer {

    private final InventoryRepository repository;

    @Bean
    CommandLineRunner loadSampleInventory() {

        return args -> {

            createIfMissing(
                    "P1001",
                    "LAPTOP-001",
                    50,
                    5
            );

            createIfMissing(
                    "P1002",
                    "PHONE-001",
                    50,
                    5
            );

            createIfMissing(
                    "P1003",
                    "HEADPHONE-001",
                    50,
                    5
            );

            createIfMissing(
                    "P1004",
                    "WATCH-001",
                    50,
                    5
            );

            createIfMissing(
                    "P1005",
                    "TABLET-001",
                    50,
                    5
            );

            createIfMissing(
                    "P1006",
                    "MONITOR-001",
                    50,
                    5
            );

            createIfMissing(
                    "P1007",
                    "KEYBOARD-001",
                    50,
                    5
            );

            createIfMissing(
                    "P1008",
                    "MOUSE-001",
                    50,
                    5
            );

            System.out.println(
                    "================================================="
            );
            System.out.println(
                    "Sample inventory initialization completed."
            );
            System.out.println(
                    "Inventory records: " + repository.count()
            );
            System.out.println(
                    "================================================="
            );
        };
    }

    private void createIfMissing(
            String productId,
            String sku,
            int availableQuantity,
            int reorderLevel) {

        if (repository.findByProductId(productId).isPresent()) {
            return;
        }

        Inventory inventory = new Inventory();

        inventory.setProductId(productId);
        inventory.setSku(sku);
        inventory.setAvailableQuantity(availableQuantity);
        inventory.setReservedQuantity(0);
        inventory.setReorderLevel(reorderLevel);
        inventory.setStatus(availableQuantity == 0 ? Inventory.InventoryStatus.OUT_OF_STOCK : (availableQuantity <= reorderLevel ? Inventory.InventoryStatus.LOW_STOCK : Inventory.InventoryStatus.AVAILABLE));
        inventory.setVersion(1L);

        repository.save(inventory);
    }
}