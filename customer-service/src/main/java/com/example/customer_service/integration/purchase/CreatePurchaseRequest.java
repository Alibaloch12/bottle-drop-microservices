package com.example.customer_service.integration.purchase;


import java.util.List;

public record CreatePurchaseRequest(
        Long visitorId,
        Long barId,
        List<Item> items
) {

    public record Item(
            Long beverageTypeId,
            int quantity
    ) {
    }
}
