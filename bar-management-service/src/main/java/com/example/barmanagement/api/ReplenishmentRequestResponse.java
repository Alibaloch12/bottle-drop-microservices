package com.example.barmanagement.api;

import com.example.barmanagement.domain.ReplenishmentRequest;

import java.math.BigDecimal;
import java.time.Instant;

public record ReplenishmentRequestResponse(
        Long id,
        Long barId,
        Long beverageTypeId,
        String beverageName,
        BigDecimal price,
        int quantityRequested,
        String status,
        Instant requestedAt,
        Instant decidedAt,
        Long warehouseRequestId
) {

    public static ReplenishmentRequestResponse from(
            ReplenishmentRequest request
    ) {
        return new ReplenishmentRequestResponse(
                request.getId(),
                request.getBarId(),
                request.getBeverageType().getId(),
                request.getBeverageType().getName(),
                request.getBeverageType().getPrice(),
                request.getQuantityRequested(),
                request.getStatus().name(),
                request.getRequestedAt(),
                request.getDecidedAt(),
                request.getWarehouseRequestId()
        );
    }
}