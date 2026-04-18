package com.mh.notification.infrastructure.gateway;

import com.mh.notification.application.exception.NotificationSendException;
import com.mh.notification.domain.FailureType;
import com.mh.notification.infrastructure.client.mock.MockApiClient;
import com.mh.notification.infrastructure.client.mock.SecondaryMockApiClient;
import com.mh.notification.infrastructure.client.mock.dto.MockApiSendRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationSendGatewayTest {

    @Mock
    private MockApiClient primaryMockApiClient;

    @Mock
    private SecondaryMockApiClient secondaryMockApiClient;

    @InjectMocks
    private NotificationSendGateway notificationSendGateway;

    @Test
    void primarySuccess_secondaryNotCalled() {
        // given
        String requestId = "req-123456789";

        MockApiSendRequest request = new MockApiSendRequest(
                requestId,
                "SMS",
                "01012345678",
                "결제 완료\n결제가 정상적으로 완료되었습니다.",
                Map.of(
                        "notificationId", 1L,
                        "channel", "SMS",
                        "retryCount", 0,
                        "title", "결제 완료"
                )
        );

        // when
        notificationSendGateway.send(request, requestId);

        // then
        verify(primaryMockApiClient).send(request, requestId);
        verify(secondaryMockApiClient, never()).send(any(), anyString());
    }

    @Test
    void primaryFail_secondaryCalled() {
        // given
        String requestId = "req-123456789";

        MockApiSendRequest request = new MockApiSendRequest(
                requestId,
                "SMS",
                "01012345678",
                "결제 완료\n결제가 정상적으로 완료되었습니다.",
                Map.of(
                        "notificationId", 1L,
                        "channel", "SMS",
                        "retryCount", 0,
                        "title", "결제 완료"
                )
        );

        NotificationSendException exception = new NotificationSendException(
                FailureType.TIMEOUT,
                null,
                "request timed out",
                null
        );

        doThrow(exception)
                .when(primaryMockApiClient)
                .send(request, requestId);

        // when
        notificationSendGateway.send(request, requestId);

        // then
        verify(primaryMockApiClient).send(request, requestId);
        verify(secondaryMockApiClient).send(request, requestId);
    }

    @Test
    void secondaryFail_throwException() {
        // given
        String requestId = "req-123456789";

        MockApiSendRequest request = new MockApiSendRequest(
                requestId,
                "SMS",
                "01012345678",
                "결제 완료\n결제가 정상적으로 완료되었습니다.",
                Map.of(
                        "notificationId", 1L,
                        "channel", "SMS",
                        "retryCount", 0,
                        "title", "결제 완료"
                )
        );

        NotificationSendException primaryException = new NotificationSendException(
                FailureType.TIMEOUT,
                null,
                "request timed out",
                null
        );

        NotificationSendException secondaryException = new NotificationSendException(
                FailureType.CONNECT_FAIL,
                null,
                "connection refused",
                null
        );

        doThrow(primaryException)
                .when(primaryMockApiClient)
                .send(request, requestId);

        doThrow(secondaryException)
                .when(secondaryMockApiClient)
                .send(request, requestId);

        // when & then
        NotificationSendException thrown = assertThrows(
                NotificationSendException.class,
                () -> notificationSendGateway.send(request, requestId)
        );

        verify(primaryMockApiClient).send(request, requestId);
        verify(secondaryMockApiClient).send(request, requestId);
        assertThat(thrown).isSameAs(secondaryException);
    }

}