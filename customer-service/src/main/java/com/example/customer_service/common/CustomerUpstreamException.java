package com.example.customer_service.common;
public class CustomerUpstreamException
        extends RuntimeException {

    private final int statusCode;

    public CustomerUpstreamException(
            int statusCode,
            String message
    ) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}