package com.mh.notification.application.outbox;

import com.mh.notification.domain.NotificationOutbox;

public interface NotificationMessagePublisher {

    void publish(NotificationOutbox outbox);
}
