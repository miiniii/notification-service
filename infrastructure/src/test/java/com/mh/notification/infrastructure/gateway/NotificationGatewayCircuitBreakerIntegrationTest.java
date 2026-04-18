package com.mh.notification.infrastructure.gateway;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class NotificationGatewayCircuitBreakerIntegrationTest {

    @Test
    void repeatedFailures_shouldOpenCircuitBreaker() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .slidingWindowSize(20)
                .minimumNumberOfCalls(10)
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofSeconds(10))
                .permittedNumberOfCallsInHalfOpenState(5)
                .build();

        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(config);
        CircuitBreaker circuitBreaker = registry.circuitBreaker("mockApi");

        for (int i = 0; i < 10; i++) {
            try {
                CircuitBreaker.decorateRunnable(circuitBreaker, () -> {
                    throw new RuntimeException("503 failure");
                }).run();
            } catch (Exception ignored) {
            }
        }
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);

        CallNotPermittedException exception = assertThrows(
                CallNotPermittedException.class,
                () -> CircuitBreaker.decorateRunnable(circuitBreaker, () -> {
                }).run()
        );

        assertThat(exception).isNotNull();
    }

    @Test
    void afterWaitDuration_shouldMoveToHalfOpen() throws Exception {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .slidingWindowSize(20)
                .minimumNumberOfCalls(10)
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofSeconds(10))
                .permittedNumberOfCallsInHalfOpenState(5)
                .build();

        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(config);
        CircuitBreaker circuitBreaker = registry.circuitBreaker("mockApi");

        for (int i = 0; i < 10; i++) {
            try {
                CircuitBreaker.decorateRunnable(circuitBreaker, () -> {
                    throw new RuntimeException("503 failure");
                }).run();
            } catch (Exception ignored) {
            }
        }

        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);

        Thread.sleep(10_500);

        try {
            CircuitBreaker.decorateRunnable(circuitBreaker, () -> {
            }).run();
        } catch (Exception ignored) {
        }

        assertThat(circuitBreaker.getState())
                .isIn(CircuitBreaker.State.HALF_OPEN, CircuitBreaker.State.CLOSED);
    }
}
