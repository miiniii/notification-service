package com.mh.notification.infrastructure.serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mh.notification.application.dto.NotificationMessage;
import com.mh.notification.application.port.NotificationMessageDeserializer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JacksonNotificationMessageDeserializer implements NotificationMessageDeserializer {

    private final ObjectMapper objectMapper;

    @Override
    public NotificationMessage deserialize(String payloadJson) {
        try {
            return objectMapper.readValue(payloadJson, NotificationMessage.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize notification message.", e);
        }
    }
}
