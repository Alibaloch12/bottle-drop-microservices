package com.example.barmanagement.common;

import com.example.barmanagement.domain.exception.ForbiddenOperationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class InternalApiKeyGuard {

    private final String expectedApiKey;

    public InternalApiKeyGuard(
            @Value("${services.internal-api-key}")
            String expectedApiKey
    ) {
        this.expectedApiKey = expectedApiKey;
    }

    public void verify(String receivedApiKey) {
        if (!Objects.equals(expectedApiKey, receivedApiKey)) {
            throw new ForbiddenOperationException(
                    "Invalid internal API key."
            );
        }
    }
}