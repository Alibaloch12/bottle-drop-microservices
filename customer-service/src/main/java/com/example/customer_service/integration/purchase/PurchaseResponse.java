package com.example.customer_service.integration.purchase;


import java.math.BigDecimal;
import java.util.List;

public record PurchaseResponse(
        Long purchaseId,
        Long visitorId,
        Long barId,
        List<Item> items,
        BigDecimal totalPrice,
        String status
) {

    public record Item(
            Long beverageTypeId,
            String beverageName,
            int quantity,
            BigDecimal unitPrice,
            BigDecimal lineTotal
    ) {
    }
}
