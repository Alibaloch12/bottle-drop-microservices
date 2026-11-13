package com.bottleneck.warehouse.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "empty_transfers")
public class EmptyTransfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "drop_point_id", nullable = false)
    private Long dropPointId;

    @Enumerated(EnumType.STRING)
    @Column(name = "beverage_type", nullable = false)
    private BeverageType beverageType;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "received_at", nullable = false)
    private LocalDateTime receivedAt;

    public EmptyTransfer() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDropPointId() {
        return dropPointId;
    }

    public void setDropPointId(Long dropPointId) {
        this.dropPointId = dropPointId;
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

    public LocalDateTime getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(LocalDateTime receivedAt) {
        this.receivedAt = receivedAt;
    }
}
