package com.example.barmanagement.domain;

import com.example.barmanagement.domain.exception.BeverageNotStockedException;
import com.example.barmanagement.domain.exception.DuplicateStockException;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "bars")
public class Bar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false, length = 200)
    private String location;

    @Version
    private long version;

    @OneToMany(
            mappedBy = "bar",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<BarBeverageStock> stocks =
            new ArrayList<>();

    protected Bar() {
        // Required by JPA
    }

    private Bar(
            String name,
            String description,
            String location
    ) {
        updateDetails(
                name,
                description,
                location
        );
    }

    public static Bar create(
            String name,
            String description,
            String location
    ) {
        return new Bar(
                name,
                description,
                location
        );
    }

    public void updateDetails(
            String name,
            String description,
            String location
    ) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Bar name is required."
            );
        }

        if (location == null || location.isBlank()) {
            throw new IllegalArgumentException(
                    "Bar location is required."
            );
        }

        this.name = name.trim();
        this.description = description == null
                ? null
                : description.trim();
        this.location = location.trim();
    }

    public void addStock(
            BeverageType beverageType,
            int initialQuantity,
            int restockThreshold
    ) {
        if (beverageType == null) {
            throw new IllegalArgumentException(
                    "Beverage type is required."
            );
        }

        if (findStock(beverageType.getId()) != null) {
            throw new DuplicateStockException(
                    beverageType.getId()
            );
        }

        stocks.add(
                new BarBeverageStock(
                        this,
                        beverageType,
                        initialQuantity,
                        restockThreshold
                )
        );
    }

    public void correctStock(
            Long beverageTypeId,
            String beverageName,
            BigDecimal price,
            int quantity,
            int restockThreshold
    ) {
        requireStock(beverageTypeId).correct(
                beverageName,
                price,
                quantity,
                restockThreshold
        );
    }

    public void removeStock(Long beverageTypeId) {
        stocks.remove(
                requireStock(beverageTypeId)
        );
    }

    public int recordSale(
            Long beverageTypeId,
            int quantity
    ) {
        BarBeverageStock stock =
                requireStock(beverageTypeId);

        stock.decrease(quantity);

        return stock.getQuantity();
    }

    public int receiveReplenishment(
            Long beverageTypeId,
            int deliveredQuantity
    ) {
        BarBeverageStock stock =
                requireStock(beverageTypeId);

        stock.increase(deliveredQuantity);

        return stock.getQuantity();
    }

    public int availableQuantity(
            Long beverageTypeId
    ) {
        return requireStock(beverageTypeId)
                .getQuantity();
    }

    public boolean isBelowThreshold(
            Long beverageTypeId
    ) {
        return requireStock(beverageTypeId)
                .isBelowThreshold();
    }

    public BeverageType getBeverageType(
            Long beverageTypeId
    ) {
        return requireStock(beverageTypeId)
                .getBeverageType();
    }

    private BarBeverageStock requireStock(
            Long beverageTypeId
    ) {
        BarBeverageStock stock =
                findStock(beverageTypeId);

        if (stock == null) {
            throw new BeverageNotStockedException(
                    beverageTypeId
            );
        }

        return stock;
    }

    private BarBeverageStock findStock(
            Long beverageTypeId
    ) {
        if (beverageTypeId == null) {
            return null;
        }

        return stocks.stream()
                .filter(stock ->
                        Objects.equals(
                                stock.getBeverageType().getId(),
                                beverageTypeId
                        )
                )
                .findFirst()
                .orElse(null);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getLocation() {
        return location;
    }

    public List<BarBeverageStock> getStocks() {
        return Collections.unmodifiableList(stocks);
    }
}