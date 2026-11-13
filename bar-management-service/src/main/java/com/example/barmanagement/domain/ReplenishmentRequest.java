package com.example.barmanagement.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "replenishment_requests")
public class ReplenishmentRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bar_id", nullable = false)
    private Long barId;

    @Embedded
    private BeverageType beverageType;

    @Column(nullable = false)
    private int quantityRequested;

    @Column
    private Integer deliveredQuantity;

    @Column
    private Instant fulfilledAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReplenishmentStatus status;

    @Column(nullable = false)
    private Instant requestedAt;

    private Instant decidedAt;

    private Long warehouseRequestId;
    @Version
    private long version;

    protected ReplenishmentRequest() {
        // Required by JPA
    }

    private ReplenishmentRequest(
            Long barId,
            BeverageType beverageType,
            int quantityRequested
    ) {
        if (barId == null || barId <= 0) {
            throw new IllegalArgumentException(
                    "Bar ID is required."
            );
        }

        if (beverageType == null) {
            throw new IllegalArgumentException(
                    "Beverage type is required."
            );
        }

        if (quantityRequested <= 0) {
            throw new IllegalArgumentException(
                    "Requested quantity must be greater than zero."
            );
        }

        this.barId = barId;
        this.beverageType = beverageType;
        this.quantityRequested = quantityRequested;
        this.status = ReplenishmentStatus.REQUESTED;
        this.requestedAt = Instant.now();
    }

    public static ReplenishmentRequest create(
            Long barId,
            BeverageType beverageType,
            int quantityRequested
    ) {
        return new ReplenishmentRequest(
                barId,
                beverageType,
                quantityRequested
        );
    }

    public void markAccepted(
            Long warehouseRequestId
    ) {
        requireStatus(ReplenishmentStatus.REQUESTED);

        this.status = ReplenishmentStatus.ACCEPTED;
        this.warehouseRequestId =
                warehouseRequestId;
        this.decidedAt = Instant.now();
    }

    public void markRejected() {
        requireStatus(ReplenishmentStatus.REQUESTED);

        this.status = ReplenishmentStatus.REJECTED;
        this.decidedAt = Instant.now();
    }

//    public void complete() {
//        if (
//                status != ReplenishmentStatus.ACCEPTED
//                        && status != ReplenishmentStatus.REQUESTED
//        ) {
//            throw new IllegalStateException(
//                    "Only a requested or accepted " +
//                            "replenishment can be completed."
//            );
//        }
//
//        this.status = ReplenishmentStatus.COMPLETED;
//        this.decidedAt = Instant.now();
//    }
public void complete(
        Long receivedWarehouseRequestId,
        int deliveredQuantity
) {
    if (receivedWarehouseRequestId == null) {
        throw new IllegalArgumentException(
                "Warehouse request ID is required."
        );
    }

    if (deliveredQuantity <= 0) {
        throw new IllegalArgumentException(
                "Delivered quantity must be greater than zero."
        );
    }

    if (status == ReplenishmentStatus.REJECTED) {
        throw new IllegalStateException(
                "A rejected replenishment cannot be completed."
        );
    }

    if (status == ReplenishmentStatus.COMPLETED) {
        if (
                Objects.equals(
                        warehouseRequestId,
                        receivedWarehouseRequestId
                )
                        && Objects.equals(
                        this.deliveredQuantity,
                        deliveredQuantity
                )
        ) {
            return;
        }

        throw new IllegalStateException(
                "The replenishment was already completed " +
                        "with different fulfillment data."
        );
    }

    /*
     * warehouseRequestId may still be null when the
     * acknowledgement response was lost but Warehouse
     * later sends a valid fulfillment callback.
     */
    if (warehouseRequestId == null) {
        warehouseRequestId =
                receivedWarehouseRequestId;

    } else if (
            !Objects.equals(
                    warehouseRequestId,
                    receivedWarehouseRequestId
            )
    ) {
        throw new IllegalStateException(
                "Warehouse request ID does not match."
        );
    }

    this.deliveredQuantity =
            deliveredQuantity;

    this.status =
            ReplenishmentStatus.COMPLETED;

    this.fulfilledAt =
            Instant.now();

    this.decidedAt =
            Instant.now();
}

    private void requireStatus(
            ReplenishmentStatus requiredStatus
    ) {
        if (status != requiredStatus) {
            throw new IllegalStateException(
                    "Expected status " + requiredStatus +
                            " but current status is " + status
            );
        }
    }

    public Long getId() {
        return id;
    }

    public Long getBarId() {
        return barId;
    }

    public BeverageType getBeverageType() {
        return beverageType;
    }

    public int getQuantityRequested() {
        return quantityRequested;
    }

    public ReplenishmentStatus getStatus() {
        return status;
    }

    public Instant getRequestedAt() {
        return requestedAt;
    }

    public Instant getDecidedAt() {
        return decidedAt;
    }

    public Long getWarehouseRequestId() {
        return warehouseRequestId;
    }
}