package com.example.barmanagement.domain.repository;

import com.example.barmanagement.domain.ProcessedSale;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedSaleRepository
        extends JpaRepository<ProcessedSale, Long> {
}