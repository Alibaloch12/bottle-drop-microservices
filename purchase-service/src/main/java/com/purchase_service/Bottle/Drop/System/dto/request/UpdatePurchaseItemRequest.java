package com.purchase_service.Bottle.Drop.System.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdatePurchaseItemRequest {

    @NotNull(message = "beverageTypeId is required")
    private Long beverageTypeId;

    @NotBlank(message = "beverageName is required")
    private String beverageName;

    private String beverageDescription;

    @NotNull(message = "quantity is required")
    @Min(value = 1, message = "quantity must be at least 1")
    private Integer quantity;

    @NotNull(message = "priceAtPurchase is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "priceAtPurchase must be greater than 0")
    private BigDecimal priceAtPurchase;
}