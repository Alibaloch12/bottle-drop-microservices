package com.example.barmanagement.api;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record ReceiveReplenishmentRequest(

        @NotNull
        @Positive
        Long warehouseReplenishmentId,

        @NotNull
        @Positive
        Long barReplenishmentRequestId,

        @NotNull
        @Positive
        Long beverageTypeId,

        @Min(1)
        int deliveredQuantity
) {
}