package com.example.customer_service.config;


import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfiguration {

    @Bean("plainRestClientBuilder")
    @Primary
    RestClient.Builder plainRestClientBuilder() {
        return RestClient.builder();
    }

    @Bean("loadBalancedRestClientBuilder")
    @LoadBalanced
    RestClient.Builder loadBalancedRestClientBuilder() {
        return RestClient.builder();
    }

    @Bean("customerBarRestClient")
    RestClient customerBarRestClient(
            @Qualifier("loadBalancedRestClientBuilder")
            RestClient.Builder builder,

            @Value("${services.bar.service-id}")
            String barServiceId,

            @Value("${services.internal-api-key}")
            String internalApiKey
    ) {
        return builder
                .clone()
                .baseUrl("http://" + barServiceId)
                .defaultHeader(
                        "X-Internal-Api-Key",
                        internalApiKey
                )
                .build();
    }

    @Bean("customerPurchaseRestClient")
    RestClient customerPurchaseRestClient(
            @Qualifier("loadBalancedRestClientBuilder")
            RestClient.Builder builder,

            @Value("${services.purchase.service-id}")
            String purchaseServiceId,

            @Value("${services.internal-api-key}")
            String internalApiKey
    ) {
        return builder
                .clone()
                .baseUrl("http://" + purchaseServiceId)
                .defaultHeader(
                        "X-Internal-Api-Key",
                        internalApiKey
                )
                .build();
    }
}