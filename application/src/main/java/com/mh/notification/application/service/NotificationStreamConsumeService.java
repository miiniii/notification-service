package com.mh.notification.application.service;

import com.mh.notification.application.dto.ConsumeResult;
import com.mh.notification.application.dto.ConsumeTaskResult;
import com.mh.notification.application.dto.NotificationMessage;
import com.mh.notification.application.dto.StreamMessage;
import com.mh.notification.application.port.NotificationMessageDeserializer;
import com.mh.notification.application.port.NotificationMessageStreamAcknowledger;
import com.mh.notification.application.port.NotificationMessageStreamDeleter;
import com.mh.notification.application.port.NotificationMessageStreamReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.info.ProjectInfoProperties;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationStreamConsumeService {

    private final NotificationMessageStreamReader notificationMessageStreamReader;
    private final NotificationMessageDeserializer notificationMessageDeserializer;
    private final NotificationMessageConsumer notificationMessageConsumer;
    private final NotificationMessageStreamAcknowledger notificationMessageStreamAcknowledger;
    private final ExecutorService notificationVirtualThreadExecutor;
    private final ProjectInfoProperties projectInfoProperties;

    public void consumeOnce() {
        List<StreamMessage> messages = notificationMessageStreamReader.readMessages();

        List<Future<ConsumeTaskResult>> futures = new ArrayList<>();

        for (StreamMessage streamMessage : messages) {
            futures.add(notificationVirtualThreadExecutor.submit(() -> {
                NotificationMessage message =
                        notificationMessageDeserializer.deserialize(streamMessage.payload());

                ConsumeResult result = notificationMessageConsumer.consume(message);

                return new ConsumeTaskResult(streamMessage.recordId(), message, result);
            }));
        }

        for (Future<ConsumeTaskResult> future : futures) {
            try {
                ConsumeTaskResult taskResult = future.get();

                if (shouldAck(taskResult.result())) {
                    notificationMessageStreamAcknowledger.ack(taskResult.recordId());

                    log.info("[ACK] notificationId={}, channel={}, result={}",
                            taskResult.message().notificationId(),
                            taskResult.message().channel(),
                            taskResult.result());
                }
            } catch (Exception e) {
                log.error("[CONSUME ERROR] virtual thread task failed", e);
            }
        }
    }

    private boolean shouldAck(ConsumeResult result) {
        return result == ConsumeResult.SUCCESS
                || result == ConsumeResult.ALREADY_PROCESSED
                || result == ConsumeResult.RETRYABLE_FAIL
                || result == ConsumeResult.DEAD_LETTERED;
    }

}
