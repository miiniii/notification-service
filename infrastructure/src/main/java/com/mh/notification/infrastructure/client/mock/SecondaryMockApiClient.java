package com.mh.notification.infrastructure.client.mock;

import com.mh.notification.application.exception.NotificationSendException;
import com.mh.notification.domain.FailureType;
import com.mh.notification.infrastructure.client.mock.dto.MockApiSendRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.http.MediaType;
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
    private static final String REQUEST_ID_HEADER = "X-Request-Id";

    private final SecondaryMockApiProperties properties;
    private final CircuitBreakerFactory<?, ?> circuitBreakerFactory;

    @Qualifier("secondaryMockApiRestClient")
    private final RestClient restClient;

    public void send(MockApiSendRequest request, String requestId) {

        circuitBreakerFactory.create(CIRCUIT_BREAKER_ID).run(
                () -> {
                    doSend(request, requestId);
                    return null;
                },
                throwable -> {
                    throw mapToNotificationSendException(throwable);
                }
        );

    }

    private void doSend(MockApiSendRequest request, String requestId) {
        restClient.post()
                .uri(properties.getSendPath())
                .contentType(MediaType.APPLICATION_JSON)
                .header(REQUEST_ID_HEADER, requestId)
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
