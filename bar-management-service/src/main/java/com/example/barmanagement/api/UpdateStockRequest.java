package com.example.barmanagement.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UpdateStockRequest(

        @NotBlank
        String beverageName,

        @NotNull
        @DecimalMin("0.00")
        @Digits(integer = 8, fraction = 2)
        BigDecimal price,

        @Min(0)
        int quantity,

        @Min(0)
        int restockThreshold
) {
}