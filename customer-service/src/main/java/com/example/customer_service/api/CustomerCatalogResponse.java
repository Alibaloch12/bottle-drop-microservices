package com.example.customer_service.api;

import java.math.BigDecimal;
import java.util.List;

public record CustomerCatalogResponse(
        Long barId,
        String barName,
        String location,
        List<Product> products
) {

    public record Product(
            Long beverageTypeId,
            String name,
            BigDecimal price,
            int availableQuantity,
            boolean available
    ) {
    }
}