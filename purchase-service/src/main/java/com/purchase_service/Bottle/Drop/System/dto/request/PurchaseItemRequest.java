package com.purchase_service.Bottle.Drop.System.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseItemRequest {

    @NotNull(message = "Beverage type id is required")
    private Long beverageTypeId;

    @NotBlank(message = "Beverage name is required")
    private String beverageName;

    private String beverageDescription;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @NotNull(message = "Price at purchase is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price at purchase must be greater than 0")
    private BigDecimal priceAtPurchase;
}