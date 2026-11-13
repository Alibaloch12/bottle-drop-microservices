package com.example.customer_service.integration.bar;


import java.math.BigDecimal;
import java.util.List;

public record BarCatalogResponse(
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