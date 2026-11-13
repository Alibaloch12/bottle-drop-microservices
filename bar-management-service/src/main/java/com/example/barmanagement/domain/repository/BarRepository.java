package com.example.barmanagement.domain.repository;

import com.example.barmanagement.domain.Bar;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BarRepository
        extends JpaRepository<Bar, Long> {

    @Override
    @EntityGraph(attributePaths = "stocks")
    Optional<Bar> findById(Long id);

    @EntityGraph(attributePaths = "stocks")
    List<Bar> findAllByOrderByNameAsc();
}