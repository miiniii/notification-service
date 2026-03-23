package com.mh.notification.application.outbox;

import com.mh.notification.application.dto.NotificationMessage;

public interface NotificationQueuePublisher {

    void publishToWork(NotificationMessage message);
    void publishToWait(NotificationMessage message);
    void publishToDead(NotificationMessage message);
}
