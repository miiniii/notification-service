package com.mh.notification.application.port;

import com.mh.notification.application.dto.NotificationMessage;

/**
 * payload JSON -> DTO
 */
public interface NotificationMessageDeserializer {

    NotificationMessage deserialize(String payloadJson);
}
