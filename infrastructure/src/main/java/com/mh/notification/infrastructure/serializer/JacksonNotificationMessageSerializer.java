package com.mh.notification.infrastructure.serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mh.notification.application.dto.NotificationMessage;
import com.mh.notification.application.outbox.NotificationMessageSerializer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JacksonNotificationMessageSerializer implements NotificationMessageSerializer {

    private final ObjectMapper objectMapper;

    @Override
    public String serialize(NotificationMessage message) {
        try {
            return objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize notification message", e);
        }
    }
}
