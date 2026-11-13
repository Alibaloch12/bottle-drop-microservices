package com.example.barmanagement.api;

import com.example.barmanagement.domain.Bar;

import java.math.BigDecimal;
import java.util.List;

public record BarCatalogResponse(
        Long barId,
        String barName,
        String location,
        List<Product> products
) {

    public static BarCatalogResponse from(
            Bar bar
    ) {
        return new BarCatalogResponse(
                bar.getId(),
                bar.getName(),
                bar.getLocation(),
                bar.getStocks()
                        .stream()
                        .map(stock ->
                                new Product(
                                        stock.getBeverageType().getId(),
                                        stock.getBeverageType().getName(),
                                        stock.getBeverageType().getPrice(),
                                        stock.getQuantity(),
                                        stock.getQuantity() > 0
                                )
                        )
                        .toList()
        );
    }

    public record Product(
            Long beverageTypeId,
            String name,
            BigDecimal price,
            int availableQuantity,
            boolean available
    ) {
    }
}