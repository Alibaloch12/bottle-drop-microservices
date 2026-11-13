package com.example.barmanagement.integration.warehouse;

import com.example.barmanagement.domain.exception.ExternalServiceException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class WarehouseClient {

    private static final String CIRCUIT_BREAKER_ID =
            "warehouseService";

    private final RestClient restClient;
    private final CircuitBreakerFactory<?, ?>
            circuitBreakerFactory;

    public WarehouseClient(
            @Qualifier("warehouseRestClient")
            RestClient restClient,

            CircuitBreakerFactory<?, ?>
                    circuitBreakerFactory
    ) {
        this.restClient = restClient;
        this.circuitBreakerFactory =
                circuitBreakerFactory;
    }

    /**
     * Protected by the warehouseService
     * Circuit Breaker.
     */
    public WarehouseReplenishmentResponse
    requestReplenishment(
            WarehouseReplenishmentRequest request
    ) {

        return circuitBreakerFactory
                .create(CIRCUIT_BREAKER_ID)
                .run(
                        () ->
                                callWarehouseManagement(
                                        request
                                ),

                        throwable ->
                                warehouseFallback(
                                        request,
                                        throwable
                                )
                );
    }

    /**
     * Performs the real synchronous REST request.
     */
    private WarehouseReplenishmentResponse
    callWarehouseManagement(
            WarehouseReplenishmentRequest request
    ) {

        try {
            WarehouseReplenishmentResponse response =
                    restClient
                            .post()
                            .uri(
                                    "/api/v1/replenishment-requests"
                            )
                            .contentType(
                                    MediaType.APPLICATION_JSON
                            )
                            .body(request)
                            .retrieve()
                            .body(
                                    WarehouseReplenishmentResponse.class
                            );

            if (response == null) {
                throw new ExternalServiceException(
                        "Warehouse Management returned " +
                                "an empty response."
                );
            }

            return response;

        } catch (
                RestClientException exception
        ) {
            throw new ExternalServiceException(
                    "Warehouse Management could not " +
                            "be reached.",
                    exception
            );
        }
    }

    /**
     * Graceful degradation:
     *
     * The local replenishment request remains
     * REQUESTED. The sale itself does not need to
     * be cancelled only because Warehouse is
     * temporarily unavailable.
     */
    private WarehouseReplenishmentResponse
    warehouseFallback(
            WarehouseReplenishmentRequest request,
            Throwable throwable
    ) {

        if (
                throwable
                        instanceof ExternalServiceException
                        externalServiceException
        ) {
            throw externalServiceException;
        }

        throw new ExternalServiceException(
                "Warehouse Management is temporarily " +
                        "unavailable. Replenishment request " +
                        request.barReplenishmentRequestId() +
                        " remains pending.",
                throwable
        );
    }
}