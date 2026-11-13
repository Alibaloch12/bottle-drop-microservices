package com.bottleneck.warehouse.config;

import com.bottleneck.warehouse.entity.*;
import com.bottleneck.warehouse.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner loadSampleData(
            WarehouseRepository warehouseRepository,
            BeverageStockRepository beverageStockRepository,
            EmptyStockRepository emptyStockRepository,
            ReplenishmentRequestRepository replenishmentRequestRepository) {

        return args -> {
            Warehouse warehouse = new Warehouse();
            warehouse.setName("Chaos Event Central Warehouse");
            warehouse.setLocation("Main hall, ground floor");
            warehouse = warehouseRepository.save(warehouse);

            saveBeverageStock(beverageStockRepository, warehouse, BeverageType.BEER, 500);
            saveBeverageStock(beverageStockRepository, warehouse, BeverageType.COLA, 300);
            saveBeverageStock(beverageStockRepository, warehouse, BeverageType.WATER, 400);
            saveBeverageStock(beverageStockRepository, warehouse, BeverageType.JUICE, 200);
            saveBeverageStock(beverageStockRepository, warehouse, BeverageType.ENERGY_DRINK, 150);

            saveEmptyStock(emptyStockRepository, warehouse, BeverageType.BEER, 80);
            saveEmptyStock(emptyStockRepository, warehouse, BeverageType.COLA, 60);
            saveEmptyStock(emptyStockRepository, warehouse, BeverageType.WATER, 45);
            saveEmptyStock(emptyStockRepository, warehouse, BeverageType.JUICE, 20);
            saveEmptyStock(emptyStockRepository, warehouse, BeverageType.ENERGY_DRINK, 15);

            ReplenishmentRequest pending = new ReplenishmentRequest();
            pending.setBarId(2L);
            pending.setBeverageType(BeverageType.BEER);
            pending.setQuantity(600);
            pending.setStatus(RequestStatus.PENDING);
            pending.setCreatedAt(LocalDateTime.now().minusHours(2));
            replenishmentRequestRepository.save(pending);

            ReplenishmentRequest fulfilled = new ReplenishmentRequest();
            fulfilled.setBarId(1L);
            fulfilled.setBeverageType(BeverageType.COLA);
            fulfilled.setQuantity(50);
            fulfilled.setStatus(RequestStatus.FULFILLED);
            fulfilled.setCreatedAt(LocalDateTime.now().minusHours(5));
            replenishmentRequestRepository.save(fulfilled);
        };
    }

    private void saveBeverageStock(
            BeverageStockRepository repository,
            Warehouse warehouse,
            BeverageType type,
            int quantity) {
        BeverageStock stock = new BeverageStock();
        stock.setWarehouse(warehouse);
        stock.setBeverageType(type);
        stock.setQuantity(quantity);
        repository.save(stock);
    }

    private void saveEmptyStock(
            EmptyStockRepository repository,
            Warehouse warehouse,
            BeverageType type,
            int quantity) {
        EmptyStock stock = new EmptyStock();
        stock.setWarehouse(warehouse);
        stock.setBeverageType(type);
        stock.setQuantity(quantity);
        repository.save(stock);
    }
}
