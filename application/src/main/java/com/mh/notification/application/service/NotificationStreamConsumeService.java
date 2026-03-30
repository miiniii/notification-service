package com.mh.notification.application.service;

import com.mh.notification.application.dto.ConsumeResult;
import com.mh.notification.application.dto.NotificationMessage;
import com.mh.notification.application.dto.StreamMessage;
import com.mh.notification.application.port.NotificationMessageDeserializer;
import com.mh.notification.application.port.NotificationMessageStreamAcknowledger;
import com.mh.notification.application.port.NotificationMessageStreamDeleter;
import com.mh.notification.application.port.NotificationMessageStreamReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationStreamConsumeService {

    private final NotificationMessageStreamReader notificationMessageStreamReader;
    private final NotificationMessageDeserializer notificationMessageDeserializer;
    private final NotificationMessageConsumer notificationMessageConsumer;
    private final NotificationMessageStreamAcknowledger notificationMessageStreamAcknowledger;

    public void consumeOnce() {
        List<StreamMessage> messages = notificationMessageStreamReader.readMessages();

        for (StreamMessage streamMessage : messages) {
            NotificationMessage message = notificationMessageDeserializer.deserialize(streamMessage.payload());

            ConsumeResult result = notificationMessageConsumer.consume(message);
            if (result == ConsumeResult.SUCCESS || result == ConsumeResult.ALREADY_PROCESSED || result == ConsumeResult.RETRYABLE_FAIL || result == ConsumeResult.DEAD_LETTERED) {
                notificationMessageStreamAcknowledger.ack(streamMessage.recordId());
            }
        }
    }
}
