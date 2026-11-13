package com.bottleneck.warehouse.dto;

import com.bottleneck.warehouse.entity.BeverageType;

public record StockItemDTO(BeverageType beverageType, int quantity) {
}
