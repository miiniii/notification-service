package com.mh.notification.infrastructure.gateway;

import com.mh.notification.application.exception.NotificationSendException;
import com.mh.notification.domain.FailureType;
import com.mh.notification.infrastructure.client.mock.MockApiClient;
import com.mh.notification.infrastructure.client.mock.SecondaryMockApiClient;
import com.mh.notification.infrastructure.client.mock.dto.MockApiSendRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationSendGateway {

    private final MockApiClient primayMockApiClient;
    private final SecondaryMockApiClient secondaryMockApiClient;

    public void send(MockApiSendRequest request, String requestId) {
        log.info("[GATEWAY] requestId={}", requestId);

        try {
            primayMockApiClient.send(request, requestId);
            return;
        } catch (NotificationSendException e) {
            if (!isFallbackTarget(e)){
                throw e;
            }
            log.warn("[FALLBACK] primary failed. requestId={}, reason={}, trying secondary", requestId, e.getMessage());

            secondaryMockApiClient.send(request, requestId);

            log.info("[FALLBACK SUCCESS] requestId={} secondary api succeeded", requestId);
        }

    }

    private boolean isFallbackTarget(NotificationSendException e) {
        if (e.getFailureType() == FailureType.TIMEOUT) {
            return true;
        }

        if (e.getFailureType() == FailureType.CONNECT_FAIL) {
            return true;
        }

        return e.getFailureType() == FailureType.HTTP_FAIL
                && e.getFailureStatusCode() != null
                && e.getFailureStatusCode() == 503;
    }
}
