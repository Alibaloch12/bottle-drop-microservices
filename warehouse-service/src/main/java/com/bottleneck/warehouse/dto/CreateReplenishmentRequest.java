package com.bottleneck.warehouse.dto;

import com.bottleneck.warehouse.entity.BeverageType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class CreateReplenishmentRequest {

    @NotNull
    private Long barId;

    @NotNull
    private BeverageType beverageType;

    @NotNull
    @Min(1)
    private Integer quantity;

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

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
