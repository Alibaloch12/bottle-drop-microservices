package com.example.barmanagement.api;

import java.math.BigDecimal;
import java.util.UUID;

public record AvailabilityResponse(
        Long barId,
        Long beverageTypeId,
        String beverageName,
        BigDecimal unitPrice,
        int availableQuantity,
        boolean available
) {
}