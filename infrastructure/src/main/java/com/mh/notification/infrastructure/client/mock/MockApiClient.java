package com.mh.notification.infrastructure.client.mock;

import com.mh.notification.application.exception.NotificationSendException;
import com.mh.notification.domain.FailureType;
import com.mh.notification.infrastructure.client.mock.dto.MockSendRequest;
import lombok.RequiredArgsConstructor;
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
public class MockApiClient {

    private final MockApiProperties mockApiProperties;

    public void send(MockSendRequest request) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(mockApiProperties.getConnectTimeoutMs());
        requestFactory.setReadTimeout(mockApiProperties.getReadTimeoutMs());

        RestClient restClient = RestClient.builder()
                .baseUrl(mockApiProperties.getBaseUrl())
                .requestFactory(requestFactory)
                .build();

        try {
            restClient.post()
                    .uri(mockApiProperties.getSendPath())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();

        } catch (RestClientResponseException e) {
            throw new NotificationSendException(
                    FailureType.HTTP_FAIL,
                    e.getStatusCode().value(),
                    resolveHttpReason(e.getStatusCode().value()), e
            );

        } catch (ResourceAccessException e) {
            throw classifyResourceAccessException(e);

        } catch (Exception e) {
            throw new NotificationSendException(
                    FailureType.CONNECT_FAIL,
                    null,
                    "unexpected client error", e
            );
        }
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