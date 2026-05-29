package com.mh.notification.application.service;

import com.mh.notification.application.outbox.NotificationMessagePublisher;
import com.mh.notification.application.port.NotificationOutboxRepository;
import com.mh.notification.domain.NotificationOutbox;
import com.mh.notification.domain.OutboxStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationOutboxRecoveryServiceTest {

    @Mock
    private NotificationOutboxRepository notificationOutboxRepository;

    @Mock
    private NotificationMessagePublisher notificationMessagePublisher;

    private NotificationOutboxRecoveryService notificationOutboxRecoveryService;

    @BeforeEach
    void setUp() {
        notificationOutboxRecoveryService = new NotificationOutboxRecoveryService(
                notificationOutboxRepository,
                notificationMessagePublisher,
                60
        );
    }

    @Test
    void republishPublishedOutboxesWithoutSendResult_findTargetsAfterGracePeriod() {
        // given
        when(notificationOutboxRepository.findPublishedWithoutSendResultCreatedBefore(any()))
                .thenReturn(List.of());

        LocalDateTime before = LocalDateTime.now();

        // when
        notificationOutboxRecoveryService.republishPublishedOutboxesWithoutSendResult();

        LocalDateTime after = LocalDateTime.now();

        // then
        ArgumentCaptor<LocalDateTime> cutoffCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(notificationOutboxRepository).findPublishedWithoutSendResultCreatedBefore(cutoffCaptor.capture());

        assertThat(cutoffCaptor.getValue()).isAfterOrEqualTo(before.minusSeconds(60));
        assertThat(cutoffCaptor.getValue()).isBeforeOrEqualTo(after.minusSeconds(60).plusSeconds(1));
    }

    @Test
    void republishPublishedOutboxesWithoutSendResult_republishPublishedOnly() {
        // given
        NotificationOutbox publishedOutbox = mock(NotificationOutbox.class);
        NotificationOutbox pendingOutbox = mock(NotificationOutbox.class);
        when(publishedOutbox.getStatus()).thenReturn(OutboxStatus.PUBLISHED);
        when(pendingOutbox.getStatus()).thenReturn(OutboxStatus.PENDING);
        when(notificationOutboxRepository.findPublishedWithoutSendResultCreatedBefore(any()))
                .thenReturn(List.of(publishedOutbox, pendingOutbox));

        // when
        notificationOutboxRecoveryService.republishPublishedOutboxesWithoutSendResult();

        // then
        verify(notificationMessagePublisher).publish(publishedOutbox);
        verify(notificationMessagePublisher, never()).publish(pendingOutbox);
    }

    @Test
    void republishPublishedOutboxesWithoutSendResult_publishFailureContinuesNextOutbox() {
        // given
        NotificationOutbox firstOutbox = mock(NotificationOutbox.class);
        NotificationOutbox secondOutbox = mock(NotificationOutbox.class);
        when(firstOutbox.getId()).thenReturn(10L);
        when(firstOutbox.getStatus()).thenReturn(OutboxStatus.PUBLISHED);
        when(secondOutbox.getStatus()).thenReturn(OutboxStatus.PUBLISHED);
        when(notificationOutboxRepository.findPublishedWithoutSendResultCreatedBefore(any()))
                .thenReturn(List.of(firstOutbox, secondOutbox));
        doThrow(new IllegalStateException("publish failed"))
                .when(notificationMessagePublisher).publish(firstOutbox);

        // when
        notificationOutboxRecoveryService.republishPublishedOutboxesWithoutSendResult();

        // then
        verify(notificationMessagePublisher).publish(firstOutbox);
        verify(notificationMessagePublisher).publish(secondOutbox);
    }

    @Test
    void republishPublishedOutboxesWithoutSendResult_successDoesNotChangeOutboxStatus() {
        // given
        NotificationOutbox outbox = mock(NotificationOutbox.class);
        when(outbox.getStatus()).thenReturn(OutboxStatus.PUBLISHED);
        when(notificationOutboxRepository.findPublishedWithoutSendResultCreatedBefore(any()))
                .thenReturn(List.of(outbox));

        // when
        notificationOutboxRecoveryService.republishPublishedOutboxesWithoutSendResult();

        // then
        verify(notificationMessagePublisher).publish(outbox);
        verify(outbox, never()).markPublished();
        verify(outbox, never()).markFailed();
    }
}
