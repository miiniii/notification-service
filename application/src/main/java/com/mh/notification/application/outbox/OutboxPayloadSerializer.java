package com.mh.notification.application.outbox;

import com.mh.notification.application.dto.NotificationOutboxPayload;

public interface OutboxPayloadSerializer {
    String serialize(NotificationOutboxPayload payload);
}
