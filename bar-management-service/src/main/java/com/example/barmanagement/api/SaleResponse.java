package com.example.barmanagement.api;

import java.math.BigDecimal;
import java.util.List;

public record SaleResponse(
        Long purchaseId,
        Long barId,
        List<Item> items,
        BigDecimal totalPrice
) {

    public record Item(
            Long beverageTypeId,
            String beverageName,
            BigDecimal unitPrice,
            int soldQuantity,
            BigDecimal lineTotal,
            int remainingQuantity,
            boolean belowRestockThreshold,
            Long replenishmentRequestId,
            String replenishmentStatus
    ) {
    }
}