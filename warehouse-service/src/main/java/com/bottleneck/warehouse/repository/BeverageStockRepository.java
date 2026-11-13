package com.bottleneck.warehouse.repository;

import com.bottleneck.warehouse.entity.BeverageStock;
import com.bottleneck.warehouse.entity.BeverageType;
import com.bottleneck.warehouse.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BeverageStockRepository extends JpaRepository<BeverageStock, Long> {

    List<BeverageStock> findByWarehouse(Warehouse warehouse);

    Optional<BeverageStock> findByWarehouseAndBeverageType(Warehouse warehouse, BeverageType beverageType);
}
