package com.mh.notification.infrastructure.client.mock;

import com.mh.notification.application.exception.NotificationSendException;
import com.mh.notification.domain.FailureType;
import com.mh.notification.infrastructure.client.mock.dto.MockSendRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.net.ConnectException;
import java.net.SocketTimeoutException;

@Component
@RequiredArgsConstructor
public class SecondaryMockApiClient {

    private static final String CIRCUIT_BREAKER_ID = "secondaryMockApi";

    private final SecondaryMockApiProperties properties;
    private final CircuitBreakerFactory<?, ?> circuitBreakerFactory;

    public void send(MockSendRequest request) {

        circuitBreakerFactory.create(CIRCUIT_BREAKER_ID).run(
                () -> {
                    doSend(request);
                    return null;
                },
                throwable -> {
                    throw mapToNotificationSendException(throwable);
                }
        );

    }

    private void doSend(MockSendRequest request) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.getConnectTimeoutMs());
        requestFactory.setReadTimeout(properties.getReadTimeoutMs());

        RestClient restClient = RestClient.builder()
                .baseUrl(properties.getBaseURL())
                .requestFactory(requestFactory)
                .build();

        restClient.post()
                .uri(properties.getSendPath())
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }

    private NotificationSendException mapToNotificationSendException(Throwable throwable) {
        Throwable cause = unwrap(throwable);

        if (cause instanceof NotificationSendException e) {
            return e;
        }

        if (cause instanceof RestClientResponseException e) {
            return new NotificationSendException(
                    FailureType.HTTP_FAIL,
                    e.getStatusCode().value(),
                    resolveHttpReason(e.getStatusCode().value()),
                    e
            );
        }

        if (cause instanceof ResourceAccessException e) {
            return classifyResourceAccessException(e);
        }

        return new NotificationSendException(
                FailureType.CONNECT_FAIL,
                null,
                "circuit breaker fallback",
                cause
        );
    }

    private Throwable unwrap(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null && current != current.getCause()) {
            current = current.getCause();
        }
        return current;
    }

    private NotificationSendException classifyResourceAccessException(ResourceAccessException e) {
        Throwable cause = e.getCause();

        if (cause instanceof SocketTimeoutException) {
            return new NotificationSendException(
                    FailureType.TIMEOUT,
                    null,
                    "request timed out", e
            );
        }

        if (cause instanceof ConnectException) {
            return new NotificationSendException(
                    FailureType.CONNECT_FAIL,
                    null,
                    "connection refused", e
            );
        }

        String message = e.getMessage() == null ? "" : e.getMessage().toLowerCase();

        if (message.contains("timed out")) {
            return new NotificationSendException(
                    FailureType.TIMEOUT,
                    null,
                    "request timed out", e
            );
        }

        return new NotificationSendException(
                FailureType.CONNECT_FAIL,
                null,
                "resource access failed", e
        );
    }

    private String resolveHttpReason(int statusCode) {
        return switch (statusCode) {
            case 429 -> "rate limited";
            case 503 -> "service unavailable";
            case 500 -> "mock internal error";
            default -> "http error";
        };
    }
}
