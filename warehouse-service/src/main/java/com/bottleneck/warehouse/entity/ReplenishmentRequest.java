package com.bottleneck.warehouse.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "replenishment_requests")
public class ReplenishmentRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bar_id", nullable = false)
    private Long barId;

    @Enumerated(EnumType.STRING)
    @Column(name = "beverage_type", nullable = false)
    private BeverageType beverageType;

    @Column(nullable = false)
    private int quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public ReplenishmentRequest() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBarId() {
        return barId;
    }

    public void setBarId(Long barId) {
        this.barId = barId;
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

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
