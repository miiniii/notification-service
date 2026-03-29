package com.mh.notification.application.service;

import com.mh.notification.application.dto.NotificationMessage;
import com.mh.notification.application.dto.StreamMessage;
import com.mh.notification.application.port.NotificationMessageDeserializer;
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
    private final NotificationMessageStreamDeleter notificationMessageStreamDeleter;

    public void consumeOnce() {
        List<StreamMessage> messages = notificationMessageStreamReader.readMessages();

        for (StreamMessage streamMessage : messages) {
            NotificationMessage message = notificationMessageDeserializer.deserialize(streamMessage.payload());
            notificationMessageConsumer.consume(message);
            notificationMessageStreamDeleter.delete(streamMessage.recordId());

        }
    }
}
