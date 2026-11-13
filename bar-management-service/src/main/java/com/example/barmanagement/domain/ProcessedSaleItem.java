package com.example.barmanagement.domain;


import com.example.barmanagement.api.SaleResponse;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;

@Embeddable
public class ProcessedSaleItem {

    @Column(nullable = false)
    private Long beverageTypeId;

    @Column(nullable = false, length = 100)
    private String beverageName;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private int soldQuantity;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal lineTotal;

    @Column(nullable = false)
    private int remainingQuantity;

    @Column(nullable = false)
    private boolean belowRestockThreshold;

    private Long replenishmentRequestId;

    @Column(nullable = false, length = 60)
    private String replenishmentStatus;

    protected ProcessedSaleItem() {
    }

    private ProcessedSaleItem(
            SaleResponse.Item item
    ) {
        this.beverageTypeId =
                item.beverageTypeId();
        this.beverageName =
                item.beverageName();
        this.unitPrice =
                item.unitPrice();
        this.soldQuantity =
                item.soldQuantity();
        this.lineTotal =
                item.lineTotal();
        this.remainingQuantity =
                item.remainingQuantity();
        this.belowRestockThreshold =
                item.belowRestockThreshold();
        this.replenishmentRequestId =
                item.replenishmentRequestId();
        this.replenishmentStatus =
                item.replenishmentStatus();
    }

    public static ProcessedSaleItem from(
            SaleResponse.Item item
    ) {
        return new ProcessedSaleItem(item);
    }

    public SaleResponse.Item toResponse() {
        return new SaleResponse.Item(
                beverageTypeId,
                beverageName,
                unitPrice,
                soldQuantity,
                lineTotal,
                remainingQuantity,
                belowRestockThreshold,
                replenishmentRequestId,
                replenishmentStatus
        );
    }

    public Long getBeverageTypeId() {
        return beverageTypeId;
    }

    public int getSoldQuantity() {
        return soldQuantity;
    }
}