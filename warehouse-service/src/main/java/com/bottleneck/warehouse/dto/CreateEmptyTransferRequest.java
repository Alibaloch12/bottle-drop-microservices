package com.bottleneck.warehouse.dto;

import com.bottleneck.warehouse.entity.BeverageType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class CreateEmptyTransferRequest {

    @NotNull
    private Long dropPointId;

    @NotNull
    private BeverageType beverageType;

    @NotNull
    @Min(1)
    private Integer quantity;

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

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
