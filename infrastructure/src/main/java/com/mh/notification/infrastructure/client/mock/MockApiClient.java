package com.mh.notification.infrastructure.client.mock;

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
            throw new IllegalStateException(
                    "MOCK_API_HTTP_FAIL: status=" + e.getStatusCode().value()
                            + ", body=" + safe(e.getResponseBodyAsString()),
                    e
            );

        } catch (ResourceAccessException e) {
            throw classifyResourceAccessException(e);

        } catch (Exception e) {
            throw new IllegalStateException("MOCK_API_CONNECT_FAIL: unexpected client error", e);
        }
    }

    private IllegalStateException classifyResourceAccessException(ResourceAccessException e) {
        Throwable cause = e.getCause();

        if (cause instanceof SocketTimeoutException) {
            return new IllegalStateException("MOCK_API_TIMEOUT: request timed out", e);
        }

        if (cause instanceof ConnectException) {
            return new IllegalStateException("MOCK_API_CONNECT_FAIL: connection refused", e);
        }

        String message = e.getMessage() == null ? "" : e.getMessage().toLowerCase();

        if (message.contains("timed out")) {
            return new IllegalStateException("MOCK_API_TIMEOUT: request timed out", e);
        }

        return new IllegalStateException("MOCK_API_CONNECT_FAIL: resource access failed", e);
    }

    private String safe(String value) {
        return (value == null || value.isBlank()) ? "empty" : value;
    }
}