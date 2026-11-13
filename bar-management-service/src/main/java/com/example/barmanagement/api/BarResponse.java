package com.example.barmanagement.api;

import com.example.barmanagement.domain.Bar;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record BarResponse(
        Long id,
        String name,
        String description,
        String location,
        List<StockResponse> stocks
) {

    public static BarResponse from(Bar bar) {
        List<StockResponse> stocks =
                bar.getStocks()
                        .stream()
                        .map(stock ->
                                new StockResponse(
                                        stock.getBeverageType().getId(),
                                        stock.getBeverageType().getName(),
                                        stock.getBeverageType().getPrice(),
                                        stock.getQuantity(),
                                        stock.getRestockThreshold(),
                                        stock.isBelowThreshold()
                                )
                        )
                        .toList();

        return new BarResponse(
                bar.getId(),
                bar.getName(),
                bar.getDescription(),
                bar.getLocation(),
                stocks
        );
    }

    public record StockResponse(
            Long beverageTypeId,
            String beverageName,
            BigDecimal price,
            int quantity,
            int restockThreshold,
            boolean belowThreshold
    ) {
    }
}