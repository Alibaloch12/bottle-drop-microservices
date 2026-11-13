package com.bottleneck.warehouse.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record DropPointEmptiesTransferRequest(
        @NotNull Long dropPointId,
        @Min(1) int quantity
) {
}
