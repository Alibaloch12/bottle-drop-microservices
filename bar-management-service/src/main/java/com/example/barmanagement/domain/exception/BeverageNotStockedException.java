package com.example.barmanagement.domain.exception;

public class BeverageNotStockedException
        extends RuntimeException {

    public BeverageNotStockedException(
            Long beverageTypeId
    ) {
        super(
                "The bar does not stock beverage type: " +
                        beverageTypeId
        );
    }
}