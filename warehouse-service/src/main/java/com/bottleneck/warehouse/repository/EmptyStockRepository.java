package com.bottleneck.warehouse.repository;

import com.bottleneck.warehouse.entity.BeverageType;
import com.bottleneck.warehouse.entity.EmptyStock;
import com.bottleneck.warehouse.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmptyStockRepository extends JpaRepository<EmptyStock, Long> {

    List<EmptyStock> findByWarehouse(Warehouse warehouse);

    Optional<EmptyStock> findByWarehouseAndBeverageType(Warehouse warehouse, BeverageType beverageType);
}
