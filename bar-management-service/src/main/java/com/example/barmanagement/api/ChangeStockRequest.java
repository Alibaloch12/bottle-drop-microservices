package com.example.barmanagement.api;

import jakarta.validation.constraints.Min;

public record ChangeStockRequest(
        @Min(1) int quantity
) {
}