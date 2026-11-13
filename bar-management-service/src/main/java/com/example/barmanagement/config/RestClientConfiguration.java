package com.example.barmanagement.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfiguration {

    /*
     * Plain RestClient builder.
     *
     * Eureka and other Spring infrastructure can use this builder
     * for normal physical URLs such as:
     *
     * http://localhost:8761/eureka/
     *
     * It must NOT be load-balanced.
     */
    @Bean("plainRestClientBuilder")
    @Primary
    RestClient.Builder plainRestClientBuilder() {
        return RestClient.builder();
    }

    /*
     * Load-balanced builder used only for business communication
     * between microservices.
     *
     * Examples:
     * http://user-management-service
     * http://warehouse-management-service
     */
    @Bean("loadBalancedRestClientBuilder")
    @LoadBalanced
    RestClient.Builder loadBalancedRestClientBuilder() {
        return RestClient.builder();
    }

    @Bean("identityRestClient")
    RestClient identityRestClient(
            @Qualifier("loadBalancedRestClientBuilder")
            RestClient.Builder builder,

            @Value("${services.identity.service-id}")
            String identityServiceId,

            @Value("${services.internal-api-key}")
            String internalApiKey
    ) {
        return builder
                .clone()
                .baseUrl("http://" + identityServiceId)
                .defaultHeader(
                        "X-Internal-Api-Key",
                        internalApiKey
                )
                .build();
    }

    @Bean("warehouseRestClient")
    RestClient warehouseRestClient(
            @Qualifier("loadBalancedRestClientBuilder")
            RestClient.Builder builder,

            @Value("${services.warehouse.service-id}")
            String warehouseServiceId,

            @Value("${services.internal-api-key}")
            String internalApiKey
    ) {
        return builder
                .clone()
                .baseUrl("http://" + warehouseServiceId)
                .defaultHeader(
                        "X-Internal-Api-Key",
                        internalApiKey
                )
                .build();
    }
}