package com.example.barmanagement.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record AddStockRequest(

        @NotNull
        Long beverageTypeId,

        @NotBlank
        @Size(max = 100)
        String beverageName,

        @NotNull
        @DecimalMin("0.00")
        @Digits(integer = 8, fraction = 2)
        BigDecimal price,

        @Min(0)
        int initialQuantity,

        @Min(0)
        int restockThreshold
) {
}