package com.mh.notification.infrastructure.client.mock;

import com.mh.notification.infrastructure.client.mock.dto.MockSendRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
@RequiredArgsConstructor
public class MockApiClient {

    private final MockApiProperties mockApiProperties;

    public void send(MockSendRequest request) {
        RestClient restClient = RestClient.builder()
                .baseUrl(mockApiProperties.getBaseUrl())
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
                    "Mock API error. status=" + e.getStatusCode().value()
                            + ", body=" + e.getResponseBodyAsString(), e
            );
        } catch (Exception e) {
            throw new IllegalStateException("Mock API call failed", e);
        }
    }
}