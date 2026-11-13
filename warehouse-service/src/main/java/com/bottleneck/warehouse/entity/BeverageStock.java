package com.bottleneck.warehouse.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "beverage_stock", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"warehouse_id", "beverage_type"})
})
public class BeverageStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Enumerated(EnumType.STRING)
    @Column(name = "beverage_type", nullable = false)
    private BeverageType beverageType;

    @Column(nullable = false)
    private int quantity;

    public BeverageStock() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Warehouse getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(Warehouse warehouse) {
        this.warehouse = warehouse;
    }

    public BeverageType getBeverageType() {
        return beverageType;
    }

    public void setBeverageType(BeverageType beverageType) {
        this.beverageType = beverageType;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
