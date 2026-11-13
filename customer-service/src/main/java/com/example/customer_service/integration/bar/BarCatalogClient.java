package com.example.customer_service.integration.bar;


import com.example.customer_service.common.CustomerDependencyException;
import com.example.customer_service.common.CustomerUpstreamException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class BarCatalogClient {

    private static final String CIRCUIT_BREAKER_ID =
            "customerBarService";

    private final RestClient restClient;
    private final CircuitBreakerFactory<?, ?>
            circuitBreakerFactory;

    public BarCatalogClient(
            @Qualifier("customerBarRestClient")
            RestClient restClient,

            CircuitBreakerFactory<?, ?>
                    circuitBreakerFactory
    ) {
        this.restClient = restClient;
        this.circuitBreakerFactory =
                circuitBreakerFactory;
    }

    public BarCatalogResponse getCatalog(
            Long barId
    ) {
        return circuitBreakerFactory
                .create(CIRCUIT_BREAKER_ID)
                .run(
                        () -> callCatalog(barId),
                        this::catalogFallback
                );
    }

    private BarCatalogResponse callCatalog(
            Long barId
    ) {
        try {
            BarCatalogResponse response =
                    restClient
                            .get()
                            .uri(
                                    "/api/v1/bars/{barId}/catalog",
                                    barId
                            )
                            .retrieve()
                            .body(BarCatalogResponse.class);

            if (response == null) {
                throw new CustomerDependencyException(
                        "Bar Management returned an empty catalog.",
                        null
                );
            }

            return response;

        } catch (
                HttpClientErrorException exception
        ) {
            throw new CustomerUpstreamException(
                    exception.getStatusCode().value(),
                    "Bar catalog request was rejected: " +
                            exception.getResponseBodyAsString()
            );

        } catch (
                RestClientException exception
        ) {
            throw new CustomerDependencyException(
                    "Bar products are temporarily unavailable.",
                    exception
            );
        }
    }

    private BarCatalogResponse catalogFallback(
            Throwable throwable
    ) {
        if (
                throwable instanceof CustomerUpstreamException
                        upstreamException
        ) {
            throw upstreamException;
        }

        if (
                throwable instanceof CustomerDependencyException
                        dependencyException
        ) {
            throw dependencyException;
        }

        throw new CustomerDependencyException(
                "Bar products are temporarily unavailable.",
                throwable
        );
    }
}