package com.mh.notification.application.service;

import com.mh.notification.application.dto.ConsumeResult;
import com.mh.notification.application.dto.ConsumeTaskResult;
import com.mh.notification.application.dto.NotificationMessage;
import com.mh.notification.application.dto.StreamMessage;
import com.mh.notification.application.outbox.NotificationQueuePublisher;
import com.mh.notification.application.port.NotificationMessageDeserializer;
import com.mh.notification.application.port.NotificationMessageStreamAcknowledger;
import com.mh.notification.application.port.NotificationMessageStreamReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.concurrent.*;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationStreamConsumeServiceTest {

    @Mock
    private NotificationMessageStreamReader notificationMessageStreamReader;

    @Mock
    private NotificationMessageDeserializer notificationMessageDeserializer;

    @Mock
    private NotificationMessageConsumer notificationMessageConsumer;

    @Mock
    private NotificationMessageStreamAcknowledger notificationMessageStreamAcknowledger;

    @Mock
    private NotificationQueuePublisher notificationQueuePublisher;

    @Mock
    private ExecutorService notificationVirtualThreadExecutor;

    @InjectMocks
    private NotificationStreamConsumeService notificationStreamConsumeService;

    @Test
    void consumeOnce_success_ack(){
        // given
        String recordId = "record-1";
        String payload = "payload-json";

        StreamMessage streamMessage = new StreamMessage(recordId, payload, 1L);
        NotificationMessage message = mock(NotificationMessage.class);

        when(notificationMessageStreamReader.readMessages())
                .thenReturn(List.of(streamMessage));

        when(notificationMessageDeserializer.deserialize(payload))
                .thenReturn(message);

        when(notificationMessageConsumer.consume(message))
                .thenReturn(ConsumeResult.SUCCESS);

        when(notificationVirtualThreadExecutor.submit(any(Callable.class)))
                .thenAnswer(invocation -> {
                    Callable<ConsumeTaskResult> task = invocation.getArgument(0);
                    ConsumeTaskResult result = task.call();
                    return CompletableFuture.completedFuture(result);
                });

        // when
        notificationStreamConsumeService.consumeOnce();

        // then
        verify(notificationMessageStreamAcknowledger).ack(recordId);
        verify(notificationQueuePublisher, never()).publishToDead(any());
    }

    @Test
    void retryableFail_ack() {
        // given
        String recordId = "record-2";
        String payload = "payload-json";

        StreamMessage streamMessage = new StreamMessage(recordId, payload, 1L);
        NotificationMessage message = mock(NotificationMessage.class);

        when(notificationMessageStreamReader.readMessages())
                .thenReturn(List.of(streamMessage));

        when(notificationMessageDeserializer.deserialize(payload))
                .thenReturn(message);

        when(notificationMessageConsumer.consume(message))
                .thenReturn(ConsumeResult.RETRYABLE_FAIL);

        when(notificationVirtualThreadExecutor.submit(any(Callable.class)))
                .thenAnswer(invocation -> {
                    Callable<ConsumeTaskResult> task = invocation.getArgument(0);
                    ConsumeTaskResult result = task.call();
                    return CompletableFuture.completedFuture(result);
                });

        // when
        notificationStreamConsumeService.consumeOnce();

        // then
        verify(notificationMessageStreamAcknowledger).ack(recordId);
        verify(notificationQueuePublisher, never()).publishToDead(any());
    }

    @Test
    void alreadyProcessed_ack() {
        // given
        String recordId = "record-3";
        String payload = "payload-json";

        StreamMessage streamMessage = new StreamMessage(recordId, payload, 1L);
        NotificationMessage message = mock(NotificationMessage.class);

        when(notificationMessageStreamReader.readMessages())
                .thenReturn(List.of(streamMessage));

        when(notificationMessageDeserializer.deserialize(payload))
                .thenReturn(message);

        when(notificationMessageConsumer.consume(message))
                .thenReturn(ConsumeResult.ALREADY_PROCESSED);

        when(notificationVirtualThreadExecutor.submit(any(Callable.class)))
                .thenAnswer(invocation -> {
                    Callable<ConsumeTaskResult> task = invocation.getArgument(0);
                    ConsumeTaskResult result = task.call();
                    return CompletableFuture.completedFuture(result);
                });

        // when
        notificationStreamConsumeService.consumeOnce();

        // then
        verify(notificationMessageStreamAcknowledger).ack(recordId);
        verify(notificationQueuePublisher, never()).publishToDead(any());
    }

    @Test
    void reclaim_deadLetter_publishAndAck() {
        // given
        String recordId = "record-dead";
        String payload = "payload-json";

        StreamMessage streamMessage = new StreamMessage(recordId, payload, 5L);
        NotificationMessage message = mock(NotificationMessage.class);

        when(notificationMessageStreamReader.reclaimPendingMessages())
                .thenReturn(List.of(streamMessage));

        when(notificationMessageDeserializer.deserialize(payload))
                .thenReturn(message);

        when(notificationVirtualThreadExecutor.submit(any(Callable.class)))
                .thenAnswer(invocation -> {
                    Callable<ConsumeTaskResult> task = invocation.getArgument(0);
                    ConsumeTaskResult result = task.call();
                    return CompletableFuture.completedFuture(result);
                });

        // when
        notificationStreamConsumeService.reclaimPendingMessages();

        // then
        verify(notificationQueuePublisher).publishToDead(message);
        verify(notificationMessageStreamAcknowledger).ack(recordId);
        verify(notificationMessageConsumer, never()).consume(any());
    }

    @Test
    void emptyMessages_noInteraction() {
        // given
        when(notificationMessageStreamReader.readMessages())
                .thenReturn(List.of());

        // when
        notificationStreamConsumeService.consumeOnce();

        // then
        verify(notificationVirtualThreadExecutor, never()).submit(any(Callable.class));
        verify(notificationMessageStreamAcknowledger, never()).ack(any());
        verify(notificationQueuePublisher, never()).publishToDead(any());
        verify(notificationMessageConsumer, never()).consume(any());
    }

    @Test
    void reclaim_underLimit_consume() {
        // given
        String recordId = "record-reclaim";
        String payload = "payload-json";

        StreamMessage streamMessage = new StreamMessage(recordId, payload, 4L);
        NotificationMessage message = mock(NotificationMessage.class);

        when(notificationMessageStreamReader.reclaimPendingMessages())
                .thenReturn(List.of(streamMessage));

        when(notificationMessageDeserializer.deserialize(payload))
                .thenReturn(message);

        when(notificationMessageConsumer.consume(message))
                .thenReturn(ConsumeResult.SUCCESS);

        when(notificationVirtualThreadExecutor.submit(any(Callable.class)))
                .thenAnswer(invocation -> {
                    Callable<ConsumeTaskResult> task = invocation.getArgument(0);
                    ConsumeTaskResult result = task.call();
                    return CompletableFuture.completedFuture(result);
                });

        // when
        notificationStreamConsumeService.reclaimPendingMessages();

        // then
        verify(notificationMessageConsumer).consume(message);
        verify(notificationMessageStreamAcknowledger).ack(recordId);
        verify(notificationQueuePublisher, never()).publishToDead(any());
    }

    @Test
    void timeout_cancelFuture() throws Exception {
        // given
        String recordId = "record-timeout";
        String payload = "payload-json";

        StreamMessage streamMessage = new StreamMessage(recordId, payload, 1L);

        @SuppressWarnings("unchecked")
        Future<ConsumeTaskResult> future = mock(Future.class);

        when(notificationMessageStreamReader.readMessages())
                .thenReturn(List.of(streamMessage));

        when(notificationVirtualThreadExecutor.submit(any(Callable.class)))
                .thenReturn(future);

        when(future.get(3L, TimeUnit.SECONDS))
                .thenThrow(new TimeoutException());

        // when
        notificationStreamConsumeService.consumeOnce();

        // then
        verify(future).cancel(true);
        verify(notificationMessageStreamAcknowledger, never()).ack(any());
        verify(notificationQueuePublisher, never()).publishToDead(any());
        verify(notificationMessageConsumer, never()).consume(any());
    }

}