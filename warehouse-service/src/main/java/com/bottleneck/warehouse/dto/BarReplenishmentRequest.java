package com.bottleneck.warehouse.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record BarReplenishmentRequest(
        Long requestId,
        @NotNull Long barId,
        Long beverageTypeId,
        String beverageName,
        @Min(1) int requestedQuantity
) {
}
