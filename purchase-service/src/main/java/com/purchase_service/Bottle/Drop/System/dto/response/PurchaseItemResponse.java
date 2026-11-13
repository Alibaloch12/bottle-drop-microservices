package com.purchase_service.Bottle.Drop.System.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseItemResponse {

    private Long id;
    private Long beverageTypeId;
    private String beverageName;
    private String beverageDescription;
    private Integer quantity;
    private BigDecimal priceAtPurchase;
}
