package com.example.barmanagement.domain.exception;

import java.util.UUID;

public class BarNotFoundException extends RuntimeException {

    public BarNotFoundException(Long barId) {
        super("Bar was not found: " + barId);
    }
}