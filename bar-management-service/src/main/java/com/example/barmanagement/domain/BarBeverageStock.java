package com.example.barmanagement.domain;

import com.example.barmanagement.domain.exception.InsufficientStockException;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(
        name = "bar_beverage_stocks",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_bar_beverage_type",
                columnNames = {
                        "bar_id",
                        "beverage_type_id"
                }
        )
)
public class BarBeverageStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private long version;

    @Embedded
    private BeverageType beverageType;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private int restockThreshold;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "bar_id",
            nullable = false
    )
    private Bar bar;

    protected BarBeverageStock() {
        // Required by JPA
    }

    BarBeverageStock(
            Bar bar,
            BeverageType beverageType,
            int quantity,
            int restockThreshold
    ) {
        if (bar == null) {
            throw new IllegalArgumentException(
                    "Bar is required."
            );
        }

        if (beverageType == null) {
            throw new IllegalArgumentException(
                    "Beverage type is required."
            );
        }

        validateQuantity(quantity);
        validateThreshold(restockThreshold);

        this.bar = bar;
        this.beverageType = beverageType;
        this.quantity = quantity;
        this.restockThreshold = restockThreshold;
    }

    void decrease(int amount) {
        requirePositiveAmount(amount);

        if (quantity < amount) {
            throw new InsufficientStockException(
                    beverageType.getId(),
                    quantity,
                    amount
            );
        }

        quantity -= amount;
    }

    void increase(int amount) {
        requirePositiveAmount(amount);
        quantity += amount;
    }

    void correct(
            String beverageName,
            BigDecimal price,
            int newQuantity,
            int newRestockThreshold
    ) {
        validateQuantity(newQuantity);
        validateThreshold(newRestockThreshold);

        this.beverageType =
                this.beverageType.withNameAndPrice(
                        beverageName,
                        price
                );

        this.quantity = newQuantity;
        this.restockThreshold =
                newRestockThreshold;
    }

    public boolean isBelowThreshold() {
        return quantity < restockThreshold;
    }

    private static void validateQuantity(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException(
                    "Stock quantity cannot be negative."
            );
        }
    }

    private static void validateThreshold(int threshold) {
        if (threshold < 0) {
            throw new IllegalArgumentException(
                    "Restock threshold cannot be negative."
            );
        }
    }

    private static void requirePositiveAmount(
            int amount
    ) {
        if (amount <= 0) {
            throw new IllegalArgumentException(
                    "Stock amount must be greater than zero."
            );
        }
    }

    public Long getId() {
        return id;
    }

    public BeverageType getBeverageType() {
        return beverageType;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getRestockThreshold() {
        return restockThreshold;
    }
}