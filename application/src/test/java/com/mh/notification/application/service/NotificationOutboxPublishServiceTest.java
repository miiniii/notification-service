package com.mh.notification.application.service;

import com.mh.notification.application.port.NotificationOutboxRepository;
import com.mh.notification.domain.NotificationOutbox;
import com.mh.notification.domain.OutboxStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationOutboxPublishServiceTest {

    @Mock
    private NotificationOutboxRepository notificationOutboxRepository;

    @Mock
    private NotificationOutboxProcessor notificationOutboxProcessor;

    private NotificationOutboxPublishService notificationOutboxPublishService;

    @BeforeEach
    void setUp() {
        notificationOutboxPublishService = new NotificationOutboxPublishService(
                notificationOutboxRepository,
                notificationOutboxProcessor
        );
    }

    @Test
    void publishPendingOutboxes_publishPendingOnly() {
        // given
        NotificationOutbox outbox = mock(NotificationOutbox.class);
        when(outbox.getId()).thenReturn(10L);
        when(notificationOutboxRepository.findAllByStatus(OutboxStatus.PENDING))
                .thenReturn(List.of(outbox));

        // when
        notificationOutboxPublishService.publishPendingOutboxes();

        // then
        verify(notificationOutboxProcessor).publishSingleOutbox(10L);
        verify(notificationOutboxRepository, never()).findPublishedWithoutSendResultCreatedBefore(any());
    }

    @Test
    void publishPendingOutboxes_publishFailureContinuesNextOutbox() {
        // given
        NotificationOutbox firstOutbox = mock(NotificationOutbox.class);
        NotificationOutbox secondOutbox = mock(NotificationOutbox.class);
        when(firstOutbox.getId()).thenReturn(10L);
        when(secondOutbox.getId()).thenReturn(20L);
        when(notificationOutboxRepository.findAllByStatus(OutboxStatus.PENDING))
                .thenReturn(List.of(firstOutbox, secondOutbox));
        doThrow(new IllegalStateException("publish failed"))
                .when(notificationOutboxProcessor).publishSingleOutbox(10L);

        // when
        notificationOutboxPublishService.publishPendingOutboxes();

        // then
        verify(notificationOutboxProcessor).publishSingleOutbox(10L);
        verify(notificationOutboxProcessor).publishSingleOutbox(20L);
    }
}
