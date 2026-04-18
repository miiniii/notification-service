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
public class MockApiClient {

    private static final String CIRCUIT_BREAKER_ID = "mockApi";
    private static final String REQUEST_ID_HEADER = "X-Request-Id";

    private final MockApiProperties mockApiProperties;
    private final CircuitBreakerFactory<?, ?> circuitBreakerFactory;

    @Qualifier("mockApiRestClient")
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
                .uri(mockApiProperties.getSendPath())
                .contentType(MediaType.APPLICATION_JSON)
                .header(REQUEST_ID_HEADER, requestId)
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }

    private NotificationSendException mapToNotificationSendException(Throwable throwable) {
        NotificationSendException sendException =
                findCause(throwable, NotificationSendException.class);
        if (sendException != null) {
            return sendException;
        }

        RestClientResponseException responseException =
                findCause(throwable, RestClientResponseException.class);
        if (responseException != null) {
            return new NotificationSendException(
                    FailureType.HTTP_FAIL,
                    responseException.getStatusCode().value(),
                    resolveHttpReason(responseException.getStatusCode().value()),
                    responseException
            );
        }

        ResourceAccessException resourceAccessException =
                findCause(throwable, ResourceAccessException.class);
        if (resourceAccessException != null) {
            return classifyResourceAccessException(resourceAccessException);
        }

        SocketTimeoutException socketTimeoutException =
                findCause(throwable, SocketTimeoutException.class);
        if (socketTimeoutException != null) {
            return new NotificationSendException(
                    FailureType.TIMEOUT,
                    null,
                    "request timed out",
                    socketTimeoutException
            );
        }

        ConnectException connectException =
                findCause(throwable, ConnectException.class);
        if (connectException != null) {
            return new NotificationSendException(
                    FailureType.CONNECT_FAIL,
                    null,
                    "connection refused",
                    connectException
            );
        }

        return new NotificationSendException(
                FailureType.CONNECT_FAIL,
                null,
                "circuit breaker fallback",
                throwable
        );
    }

    private <T extends Throwable> T findCause(Throwable throwable, Class<T> type) {
        Throwable current = throwable;
        while (current != null) {
            if (type.isInstance(current)) {
                return type.cast(current);
            }
            current = current.getCause();
        }
        return null;
    }

    private NotificationSendException classifyResourceAccessException(ResourceAccessException e) {
        Throwable cause = e.getCause();

        if (cause instanceof SocketTimeoutException) {
            return new NotificationSendException(
                    FailureType.TIMEOUT,
                    null,
                    "request timed out",
                    e
            );
        }

        if (cause instanceof ConnectException) {
            return new NotificationSendException(
                    FailureType.CONNECT_FAIL,
                    null,
                    "connection refused",
                    e
            );
        }

        String message = e.getMessage() == null ? "" : e.getMessage().toLowerCase();

        if (message.contains("timed out")
                || message.contains("timeout")
                || message.contains("read timed out")) {
            return new NotificationSendException(
                    FailureType.TIMEOUT,
                    null,
                    "request timed out",
                    e
            );
        }

        return new NotificationSendException(
                FailureType.CONNECT_FAIL,
                null,
                "resource access failed",
                e
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