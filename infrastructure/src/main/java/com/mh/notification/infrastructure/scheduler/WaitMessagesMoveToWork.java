package com.mh.notification.infrastructure.scheduler;

import com.mh.notification.application.dto.NotificationMessage;
import com.mh.notification.application.dto.StreamMessage;
import com.mh.notification.application.outbox.NotificationQueuePublisher;
import com.mh.notification.application.port.NotificationMessageDeserializer;
import com.mh.notification.application.port.WaitMessageStreamDeleter;
import com.mh.notification.application.port.WaitMessageStreamReader;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class WaitMessagesMoveToWork {

    private final WaitMessageStreamReader waitMessageStreamReader;
    private final WaitMessageStreamDeleter waitMessageStreamDeleter;
    private final NotificationMessageDeserializer notificationMessageDeserializer;
    private final NotificationQueuePublisher notificationQueuePublisher;

    @Scheduled(fixedDelay = 10000)
    public void moveMessagesToWork() {
        List<StreamMessage> messages = waitMessageStreamReader.readMessages();

        for (StreamMessage streamMessage : messages) {
            NotificationMessage message = notificationMessageDeserializer.deserialize(streamMessage.payload());

            if (message.nextRetryAt() == null) continue;

            if (message.nextRetryAt().isAfter(LocalDateTime.now())) continue;

            notificationQueuePublisher.publishToWork(message);
            waitMessageStreamDeleter.delete(streamMessage.recordId());
        }
    }
}
