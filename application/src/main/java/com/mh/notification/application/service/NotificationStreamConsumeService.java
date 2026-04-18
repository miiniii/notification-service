package com.mh.notification.application.service;

import com.mh.notification.application.dto.ConsumeResult;
import com.mh.notification.application.dto.ConsumeTaskResult;
import com.mh.notification.application.dto.NotificationMessage;
import com.mh.notification.application.dto.StreamMessage;
import com.mh.notification.application.outbox.NotificationQueuePublisher;
import com.mh.notification.application.port.NotificationMessageDeserializer;
import com.mh.notification.application.port.NotificationMessageStreamAcknowledger;
import com.mh.notification.application.port.NotificationMessageStreamReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationStreamConsumeService {

    private static final long MAX_PEL_DELIVERY_COUNT = 5L;
    private static final long TASK_TIMEOUT_SECONDS = 3L;

    private final NotificationMessageStreamReader notificationMessageStreamReader;
    private final NotificationMessageDeserializer notificationMessageDeserializer;
    private final NotificationMessageConsumer notificationMessageConsumer;
    private final NotificationMessageStreamAcknowledger notificationMessageStreamAcknowledger;
    private final NotificationQueuePublisher notificationQueuePublisher;
    private final ExecutorService notificationVirtualThreadExecutor;


    public void consumeOnce() {
        List<StreamMessage> messages = notificationMessageStreamReader.readMessages();
        processMessages(messages, false);
    }

    public void reclaimPendingMessages() {
        List<StreamMessage> messages = notificationMessageStreamReader.reclaimPendingMessages();
        processMessages(messages, true);
    }

    private void processMessages(List<StreamMessage> messages, boolean reclaimed) {
        List<Future<ConsumeTaskResult>> futures = new ArrayList<>();

        for (StreamMessage streamMessage : messages) {
            futures.add(notificationVirtualThreadExecutor.submit(() -> processSingleMessage(streamMessage, reclaimed)));
        }

        for (Future<ConsumeTaskResult> future : futures) {
            try {
                ConsumeTaskResult taskResult = future.get(TASK_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                handleTaskResult(taskResult, reclaimed);
            } catch (TimeoutException e) {
                future.cancel(true);
                log.error("[{} TIMEOUT] consume task exceeded {} seconds",
                        reclaimed ? "RECLAIM" : "CONSUME",
                        TASK_TIMEOUT_SECONDS, e);
            } catch (Exception e) {
                log.error("[{} ERROR] virtual thread task failed",
                        reclaimed ? "RECLAIM" : "CONSUME", e);
            }
        }
    }

    private ConsumeTaskResult processSingleMessage(StreamMessage streamMessage, boolean reclaimed) {
        NotificationMessage message =
                notificationMessageDeserializer.deserialize(streamMessage.payload());

        if (reclaimed && streamMessage.deliveryCount() >= MAX_PEL_DELIVERY_COUNT) {
            return new ConsumeTaskResult(
                    streamMessage.recordId(),
                    message,
                    ConsumeResult.DEAD_LETTERED,
                    streamMessage.deliveryCount()
            );
        }

        ConsumeResult result = notificationMessageConsumer.consume(message);

        return new ConsumeTaskResult(
                streamMessage.recordId(),
                message,
                result,
                streamMessage.deliveryCount()
        );
    }

    private void handleTaskResult(ConsumeTaskResult taskResult, boolean reclaimed) {
        if (taskResult.result() == ConsumeResult.DEAD_LETTERED) {
            notificationQueuePublisher.publishToDead(taskResult.message());

            notificationMessageStreamAcknowledger.ack(taskResult.recordId());

            log.warn("[{} DEAD] notificationId={}, channel={}, deliveryCount={}, recordId={}",
                    reclaimed ? "RECLAIM" : "CONSUME",
                    taskResult.message().notificationId(),
                    taskResult.message().channel(),
                    taskResult.deliveryCount(),
                    taskResult.recordId());
            return;
        }

        if (shouldAck(taskResult.result())) {
            notificationMessageStreamAcknowledger.ack(taskResult.recordId());

            log.info("[{} ACK] notificationId={}, channel={}, result={}, deliveryCount={}",
                    reclaimed ? "RECLAIM" : "CONSUME",
                    taskResult.message().notificationId(),
                    taskResult.message().channel(),
                    taskResult.result(),
                    taskResult.deliveryCount());
        }
    }

    private boolean shouldAck(ConsumeResult result) {
        return result == ConsumeResult.SUCCESS
                || result == ConsumeResult.ALREADY_PROCESSED
                || result == ConsumeResult.RETRYABLE_FAIL
                || result == ConsumeResult.DEAD_LETTERED;
    }

}
