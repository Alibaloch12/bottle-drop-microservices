package com.example.barmanagement.api;

import java.math.BigDecimal;
import java.util.List;

public record BatchAvailabilityResponse(
        Long barId,
        boolean allAvailable,
        List<Item> items
) {

    public record Item(
            Long beverageTypeId,
            String beverageName,
            BigDecimal unitPrice,
            int requestedQuantity,
            int availableQuantity,
            boolean available
    ) {
    }
}