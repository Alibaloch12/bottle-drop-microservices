package com.example.barmanagement.domain.exception;

public class InsufficientStockException
        extends RuntimeException {

    public InsufficientStockException(
            Long beverageTypeId,
            int available,
            int requested
    ) {
        super(
                "Insufficient stock for beverage type " +
                        beverageTypeId +
                        ". Available: " + available +
                        ", requested: " + requested
        );
    }
}