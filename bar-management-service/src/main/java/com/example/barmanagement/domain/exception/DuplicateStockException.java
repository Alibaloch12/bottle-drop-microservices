package com.example.barmanagement.domain.exception;

public class DuplicateStockException
        extends RuntimeException {

    public DuplicateStockException(
            Long beverageTypeId
    ) {
        super(
                "A stock entry already exists for beverage type: " +
                        beverageTypeId
        );
    }
}