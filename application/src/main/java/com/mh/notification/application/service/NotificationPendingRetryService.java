package com.mh.notification.application.service;

import com.mh.notification.application.dto.ConsumeResult;
import com.mh.notification.application.dto.ConsumeTaskResult;
import com.mh.notification.application.dto.NotificationMessage;
import com.mh.notification.application.dto.StreamMessage;
import com.mh.notification.application.port.NotificationMessageDeserializer;
import com.mh.notification.application.port.NotificationMessageStreamAcknowledger;
import com.mh.notification.application.port.NotificationPendingMessageReclaimer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationPendingRetryService {

    private final NotificationPendingMessageReclaimer notificationPendingMessageReclaimer;
    private final NotificationMessageDeserializer notificationMessageDeserializer;
    private final NotificationMessageConsumer notificationMessageConsumer;
    private final NotificationMessageStreamAcknowledger notificationMessageStreamAcknowledger;
    private final ExecutorService notificationVirtualThreadExecutor;

    public void retryPendingMessages() {
        List<StreamMessage> pendingMessages = notificationPendingMessageReclaimer.reclaimPendingMessages();

        if (pendingMessages.isEmpty()) {
            return;
        }

        log.info("[PEL RETRY] pending message count={}", pendingMessages.size());

        List<Future<ConsumeTaskResult>> futures = new ArrayList<>();

        for (StreamMessage streamMessage : pendingMessages) {
            futures.add(notificationVirtualThreadExecutor.submit(() -> processMessage(streamMessage)));
        }

        for (Future<ConsumeTaskResult> future : futures) {
            try {
                ConsumeTaskResult taskResult = future.get();

                if (shouldAck(taskResult.result())) {
                    notificationMessageStreamAcknowledger.ack(taskResult.recordId());

                    log.info("[PEL RETRY ACK] notificationId={}, channel={}, result={}",
                            taskResult.message().notificationId(),
                            taskResult.message().channel(),
                            taskResult.result());
                } else {
                    log.info("[PEL RETRY KEEP] notificationId={}, channel={}, result={}",
                            taskResult.message().notificationId(),
                            taskResult.message().channel(),
                            taskResult.result());
                }
            } catch (Exception e) {
                log.error("[PEL RETRY ERROR] virtual thread task failed", e);
            }
        }
    }

    private ConsumeTaskResult processMessage(StreamMessage streamMessage) {
        NotificationMessage message =
                notificationMessageDeserializer.deserialize(streamMessage.payload());

        ConsumeResult result = notificationMessageConsumer.consume(message);

        return new ConsumeTaskResult(streamMessage.recordId(), message, result);
    }

    private boolean shouldAck(ConsumeResult result) {
        return result == ConsumeResult.SUCCESS
                || result == ConsumeResult.ALREADY_PROCESSED
                || result == ConsumeResult.RETRYABLE_FAIL
                || result == ConsumeResult.DEAD_LETTERED;
    }
}