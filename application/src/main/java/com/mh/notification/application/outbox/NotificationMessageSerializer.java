package com.mh.notification.application.outbox;

import com.mh.notification.application.dto.NotificationMessage;

public interface NotificationMessageSerializer {
    String serialize(NotificationMessage message);
}
