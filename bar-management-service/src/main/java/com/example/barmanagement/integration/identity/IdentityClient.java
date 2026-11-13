package com.example.barmanagement.integration.identity;

import com.example.barmanagement.domain.exception.ExternalServiceException;
import com.example.barmanagement.domain.exception.ForbiddenOperationException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class IdentityClient {

    private static final String CIRCUIT_BREAKER_ID =
            "identityService";

    private final RestClient restClient;
    private final CircuitBreakerFactory<?, ?>
            circuitBreakerFactory;

    public IdentityClient(
            @Qualifier("identityRestClient")
            RestClient restClient,

            CircuitBreakerFactory<?, ?>
                    circuitBreakerFactory
    ) {
        this.restClient = restClient;
        this.circuitBreakerFactory =
                circuitBreakerFactory;
    }

    /**
     * Protected by the identityService Circuit Breaker.
     */
    public UserResponse getUser(Long userId) {
        return circuitBreakerFactory
                .create(CIRCUIT_BREAKER_ID)
                .run(
                        () -> callUserManagement(userId),
                        throwable ->
                                identityFallback(
                                        userId,
                                        throwable
                                )
                );
    }

    /**
     * Performs the real synchronous REST request.
     */
    private UserResponse callUserManagement(
            Long userId
    ) {
        try {
            UserResponse response = restClient
                    .get()
                    .uri(
                            "/api/v1/users/{userId}",
                            userId
                    )
                    .retrieve()
                    .body(UserResponse.class);

            if (response == null) {
                throw new ExternalServiceException(
                        "User Management returned an empty response."
                );
            }

            return response;

        } catch (
                HttpClientErrorException.NotFound exception
        ) {
            /*
             * A nonexistent user is a business result.
             * It should not be treated as a technical
             * User Management outage.
             */
            throw new ForbiddenOperationException(
                    "User does not exist: " + userId
            );

        } catch (
                HttpClientErrorException exception
        ) {
            throw new ExternalServiceException(
                    "User Management rejected the request. " +
                            "HTTP status: " +
                            exception.getStatusCode(),
                    exception
            );

        } catch (
                RestClientException exception
        ) {
            throw new ExternalServiceException(
                    "User Management could not be reached.",
                    exception
            );
        }
    }

    /**
     * Fail-closed policy:
     *
     * If User Management is unavailable, Bar Management
     * must not grant permission because the role cannot
     * be verified.
     */
    private UserResponse identityFallback(
            Long userId,
            Throwable throwable
    ) {
        if (
                throwable
                        instanceof ForbiddenOperationException
                        forbiddenOperationException
        ) {
            throw forbiddenOperationException;
        }

        if (
                throwable
                        instanceof ExternalServiceException
                        externalServiceException
        ) {
            throw externalServiceException;
        }

        throw new ExternalServiceException(
                "User Management is temporarily unavailable. " +
                        "The role of user " + userId +
                        " cannot be verified.",
                throwable
        );
    }

    public void requireAnyRole(
            Long userId,
            String... allowedRoles
    ) {
        UserResponse user = getUser(userId);

        if (!user.active()) {
            throw new ForbiddenOperationException(
                    "The user account is inactive."
            );
        }

        Set<String> allowed =
                Arrays.stream(allowedRoles)
                        .map(String::toUpperCase)
                        .collect(Collectors.toSet());

        String actualRole =
                user.role() == null
                        ? ""
                        : user.role().toUpperCase();

        if (!allowed.contains(actualRole)) {
            throw new ForbiddenOperationException(
                    "Required role: " + allowed +
                            ", but user has role: " +
                            actualRole
            );
        }
    }
}