package com.example.customer_service.common;


public class CustomerDependencyException
        extends RuntimeException {

    public CustomerDependencyException(
            String message,
            Throwable cause
    ) {
        super(message, cause);
    }
}
