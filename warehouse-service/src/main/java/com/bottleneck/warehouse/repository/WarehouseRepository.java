package com.bottleneck.warehouse.repository;

import com.bottleneck.warehouse.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {
}
