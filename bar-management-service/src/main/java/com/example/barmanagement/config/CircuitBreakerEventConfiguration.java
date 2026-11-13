package com.example.barmanagement.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CircuitBreakerEventConfiguration {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    CircuitBreakerEventConfiguration.class
            );

    @Bean
    Customizer<Resilience4JCircuitBreakerFactory>
    circuitBreakerEventLogger() {

        return factory ->
                factory.addCircuitBreakerCustomizer(
                        circuitBreaker ->
                                circuitBreaker
                                        .getEventPublisher()

                                        .onStateTransition(event ->
                                                LOGGER.warn(
                                                        "Circuit breaker '{}' changed state: {}",
                                                        event.getCircuitBreakerName(),
                                                        event.getStateTransition()
                                                )
                                        )

                                        .onError(event ->
                                                LOGGER.warn(
                                                        "Circuit breaker '{}' recorded an error: {}",
                                                        event.getCircuitBreakerName(),
                                                        event.getThrowable()
                                                                .getMessage()
                                                )
                                        )

                                        .onCallNotPermitted(event ->
                                                LOGGER.warn(
                                                        "Circuit breaker '{}' rejected a call " +
                                                                "because the circuit is OPEN.",
                                                        event.getCircuitBreakerName()
                                                )
                                        ),

                        "identityService",
                        "warehouseService"
                );
    }
}