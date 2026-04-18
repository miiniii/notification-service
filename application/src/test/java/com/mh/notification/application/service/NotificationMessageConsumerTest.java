package com.mh.notification.application.service;

import com.mh.notification.application.dto.ConsumeResult;
import com.mh.notification.application.dto.NotificationMessage;
import com.mh.notification.application.exception.NotificationSendException;
import com.mh.notification.application.outbox.NotificationQueuePublisher;
import com.mh.notification.application.port.NotificationDistributedLockManager;
import com.mh.notification.application.port.NotificationSendResultRepository;
import com.mh.notification.application.sender.NotificationSender;
import com.mh.notification.domain.FailureType;
import com.mh.notification.domain.NotificationChannel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationMessageConsumerTest {

    @Mock
    private NotificationSendResultRepository notificationSendResultRepository;

    @Mock
    private NotificationQueuePublisher notificationQueuePublisher;

    @Mock
    private NotificationDistributedLockManager notificationDistributedLockManager;

    @Mock
    private NotificationSender notificationSender;

    private NotificationMessageConsumer notificationMessageConsumer;

    @BeforeEach
    void setUp() {
        notificationMessageConsumer = new NotificationMessageConsumer(
                notificationSendResultRepository,
                notificationQueuePublisher,
                notificationDistributedLockManager,
                List.of(notificationSender)
        );
    }

    @Test
    void defaultFail_backoff30Seconds_andPublishToWait() {
        // given
        NotificationMessage message = new NotificationMessage(
                1L,
                "req-123456789",
                1001L,
                2001L,
                "PAYMENT",
                NotificationChannel.SMS,
                "결제 완료",
                "결제가 정상적으로 완료되었습니다.",
                "/payments/123",
                "01012345678",
                0,
                null
        );

        when(notificationDistributedLockManager.tryLock(anyString(), anyLong(), anyLong()))
                .thenReturn(true);

        when(notificationSendResultRepository.existsSuccessByNotificationIdAndChannel(
                message.notificationId(),
                message.channel()
        )).thenReturn(false);

        when(notificationSender.supports(message.channel()))
                .thenReturn(true);

        doThrow(new NotificationSendException(
                FailureType.HTTP_FAIL,
                503,
                "service unavailable",
                null
        )).when(notificationSender).send(message);

        LocalDateTime before = LocalDateTime.now();

        // when
        ConsumeResult result = notificationMessageConsumer.consume(message);

        LocalDateTime after = LocalDateTime.now();

        // then
        assertThat(result).isEqualTo(ConsumeResult.RETRYABLE_FAIL);

        ArgumentCaptor<NotificationMessage> retryCaptor =
                ArgumentCaptor.forClass(NotificationMessage.class);

        verify(notificationQueuePublisher).publishToWait(retryCaptor.capture());

        NotificationMessage retryMessage = retryCaptor.getValue();

        assertThat(retryMessage.notificationId()).isEqualTo(1L);
        assertThat(retryMessage.requestId()).isEqualTo("req-123456789");
        assertThat(retryMessage.retryCount()).isEqualTo(1);
        assertThat(retryMessage.nextRetryAt()).isNotNull();

        assertThat(retryMessage.nextRetryAt()).isAfterOrEqualTo(before.plusSeconds(30));
        assertThat(retryMessage.nextRetryAt()).isBeforeOrEqualTo(after.plusSeconds(30).plusSeconds(1));

        verify(notificationQueuePublisher, never()).publishToDead(any());
        verify(notificationDistributedLockManager).unlock("lock:notification:1:SMS");
        verify(notificationSendResultRepository).save(any());
    }

    @Test
    void rateLimit429_backoff_andPublishToWait() {
        // given
        NotificationMessage message = new NotificationMessage(
                1L,
                "req-123456789",
                1001L,
                2001L,
                "PAYMENT",
                NotificationChannel.SMS,
                "결제 완료",
                "결제가 정상적으로 완료되었습니다.",
                "/payments/123",
                "01012345678",
                0,
                null
        );

        when(notificationDistributedLockManager.tryLock(anyString(), anyLong(), anyLong()))
                .thenReturn(true);

        when(notificationSendResultRepository.existsSuccessByNotificationIdAndChannel(
                message.notificationId(),
                message.channel()
        )).thenReturn(false);

        when(notificationSender.supports(message.channel()))
                .thenReturn(true);

        doThrow(new NotificationSendException(
                FailureType.HTTP_FAIL,
                429,
                "rate limited",
                null
        )).when(notificationSender).send(message);

        LocalDateTime before = LocalDateTime.now();

        // when
        ConsumeResult result = notificationMessageConsumer.consume(message);

        LocalDateTime after = LocalDateTime.now();

        // then
        assertThat(result).isEqualTo(ConsumeResult.RETRYABLE_FAIL);

        ArgumentCaptor<NotificationMessage> retryCaptor =
                ArgumentCaptor.forClass(NotificationMessage.class);

        verify(notificationQueuePublisher).publishToWait(retryCaptor.capture());

        NotificationMessage retryMessage = retryCaptor.getValue();

        assertThat(retryMessage.notificationId()).isEqualTo(1L);
        assertThat(retryMessage.requestId()).isEqualTo("req-123456789");
        assertThat(retryMessage.retryCount()).isEqualTo(1);
        assertThat(retryMessage.nextRetryAt()).isNotNull();

        assertThat(retryMessage.nextRetryAt()).isAfterOrEqualTo(before.plusSeconds(60));
        assertThat(retryMessage.nextRetryAt()).isBeforeOrEqualTo(after.plusSeconds(60).plusSeconds(1));

        verify(notificationQueuePublisher, never()).publishToDead(any());
        verify(notificationDistributedLockManager).unlock("lock:notification:1:SMS");
        verify(notificationSendResultRepository).save(any());

    }

    @Test
    void maxRetryCount_deadLetter() {
        // given
        NotificationMessage message = new NotificationMessage(
                1L,
                "req-123456789",
                1001L,
                2001L,
                "PAYMENT",
                NotificationChannel.SMS,
                "결제 완료",
                "결제가 정상적으로 완료되었습니다.",
                "/payments/123",
                "01012345678",
                3,
                null
        );

        when(notificationDistributedLockManager.tryLock(anyString(), anyLong(), anyLong()))
                .thenReturn(true);

        when(notificationSendResultRepository.existsSuccessByNotificationIdAndChannel(
                message.notificationId(),
                message.channel()
        )).thenReturn(false);

        when(notificationSender.supports(message.channel()))
                .thenReturn(true);

        doThrow(new NotificationSendException(
                FailureType.HTTP_FAIL,
                503,
                "service unavailable",
                null
        )).when(notificationSender).send(message);

        // when
        ConsumeResult result = notificationMessageConsumer.consume(message);

        // then
        assertThat(result).isEqualTo(ConsumeResult.DEAD_LETTERED);

        verify(notificationQueuePublisher).publishToDead(message);
        verify(notificationQueuePublisher, never()).publishToWait(any());
        verify(notificationDistributedLockManager).unlock("lock:notification:1:SMS");
        verify(notificationSendResultRepository).save(any());
    }

    @Test
    void alreadySent_thenAlreadyProcessed() {
        // given
        NotificationMessage message = new NotificationMessage(
                1L,
                "req-123456789",
                1001L,
                2001L,
                "PAYMENT",
                NotificationChannel.SMS,
                "결제 완료",
                "결제가 정상적으로 완료되었습니다.",
                "/payments/123",
                "01012345678",
                0,
                null
        );

        when(notificationDistributedLockManager.tryLock(anyString(), anyLong(), anyLong()))
                .thenReturn(true);

        when(notificationSendResultRepository.existsSuccessByNotificationIdAndChannel(
                message.notificationId(),
                message.channel()
        )).thenReturn(true);

        // when
        ConsumeResult result = notificationMessageConsumer.consume(message);

        // then
        assertThat(result).isEqualTo(ConsumeResult.ALREADY_PROCESSED);
        verify(notificationSender, never()).send(any());
        verify(notificationQueuePublisher, never()).publishToWait(any());
        verify(notificationQueuePublisher, never()).publishToDead(any());
        verify(notificationDistributedLockManager).unlock("lock:notification:1:SMS");
    }

    @Test
    void lockFail_thenLockFailed() {
        // given
        NotificationMessage message = new NotificationMessage(
                1L,
                "req-123456789",
                1001L,
                2001L,
                "PAYMENT",
                NotificationChannel.SMS,
                "결제 완료",
                "결제가 정상적으로 완료되었습니다.",
                "/payments/123",
                "01012345678",
                0,
                null
        );

        when(notificationDistributedLockManager.tryLock(anyString(), anyLong(), anyLong()))
                .thenReturn(false);

        // when
        ConsumeResult result = notificationMessageConsumer.consume(message);

        // then
        assertThat(result).isEqualTo(ConsumeResult.LOCK_FAILED);
        verify(notificationSendResultRepository, never()).existsSuccessByNotificationIdAndChannel(anyLong(), any());
        verify(notificationSender, never()).send(any());
        verify(notificationQueuePublisher, never()).publishToWait(any());
        verify(notificationQueuePublisher, never()).publishToDead(any());
        verify(notificationDistributedLockManager, never()).unlock(anyString());
    }

    @Test
    void interruptedThread_thenLockFailed() {
        // given
        NotificationMessage message = new NotificationMessage(
                1L,
                "req-123456789",
                1001L,
                2001L,
                "PAYMENT",
                NotificationChannel.SMS,
                "결제 완료",
                "결제가 정상적으로 완료되었습니다.",
                "/payments/123",
                "01012345678",
                0,
                null
        );

        Thread.currentThread().interrupt();

        try {
            // when
            ConsumeResult result = notificationMessageConsumer.consume(message);

            // then
            assertThat(result).isEqualTo(ConsumeResult.LOCK_FAILED);
            verify(notificationDistributedLockManager, never()).tryLock(anyString(), anyLong(), anyLong());
            verify(notificationSender, never()).send(any());
            verify(notificationQueuePublisher, never()).publishToWait(any());
            verify(notificationQueuePublisher, never()).publishToDead(any());
        } finally {
            Thread.interrupted(); // 인터럽트 상태 초기화
        }
    }
}