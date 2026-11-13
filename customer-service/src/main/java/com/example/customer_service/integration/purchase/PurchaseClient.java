package com.example.customer_service.integration.purchase;


import com.example.customer_service.common.CustomerDependencyException;
import com.example.customer_service.common.CustomerUpstreamException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class PurchaseClient {

    private static final String CIRCUIT_BREAKER_ID =
            "customerPurchaseService";

    private final RestClient restClient;
    private final CircuitBreakerFactory<?, ?>
            circuitBreakerFactory;

    public PurchaseClient(
            @Qualifier("customerPurchaseRestClient")
            RestClient restClient,

            CircuitBreakerFactory<?, ?>
                    circuitBreakerFactory
    ) {
        this.restClient = restClient;
        this.circuitBreakerFactory =
                circuitBreakerFactory;
    }

    public PurchaseResponse createPurchase(
            CreatePurchaseRequest request
    ) {
        return circuitBreakerFactory
                .create(CIRCUIT_BREAKER_ID)
                .run(
                        () -> callPurchaseService(request),
                        this::purchaseFallback
                );
    }

    private PurchaseResponse callPurchaseService(
            CreatePurchaseRequest request
    ) {
        try {
            PurchaseResponse response =
                    restClient
                            .post()
                            .uri("/api/purchases/submit")
                            .contentType(
                                    MediaType.APPLICATION_JSON
                            )
                            .body(request)
                            .retrieve()
                            .body(PurchaseResponse.class);

            if (response == null) {
                throw new CustomerDependencyException(
                        "Purchase Service returned an empty response.",
                        null
                );
            }

            return response;

        } catch (
                HttpClientErrorException exception
        ) {
            throw new CustomerUpstreamException(
                    exception.getStatusCode().value(),
                    "Purchase was rejected: " +
                            exception.getResponseBodyAsString()
            );

        } catch (
                RestClientException exception
        ) {
            throw new CustomerDependencyException(
                    "The purchase service is temporarily unavailable.",
                    exception
            );
        }
    }

    private PurchaseResponse purchaseFallback(
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
                "The order could not be processed.",
                throwable
        );
    }
}