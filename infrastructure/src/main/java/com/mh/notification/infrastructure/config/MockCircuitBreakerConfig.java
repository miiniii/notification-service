package com.mh.notification.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@Slf4j
public class MockCircuitBreakerConfig {

    @Bean
    public Customizer<Resilience4JCircuitBreakerFactory> mockApiCircuitBreakerCustomizer() {
        return factory -> factory.addCircuitBreakerCustomizer(
                circuitBreaker -> circuitBreaker.getEventPublisher()
                        .onStateTransition(event -> log.warn("[CB STATE] {}", event))
                        .onError(event -> log.warn("[CB ERROR] {}", event))
                        .onSuccess(event -> log.info("[CB SUCCESS] {}", event)), "mockApi"
        );
    }
}
