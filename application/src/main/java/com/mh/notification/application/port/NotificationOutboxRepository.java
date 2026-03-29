package com.mh.notification.application.port;

import com.mh.notification.domain.NotificationOutbox;
import com.mh.notification.domain.OutboxStatus;

import java.util.List;

public interface NotificationOutboxRepository {

    NotificationOutbox save(NotificationOutbox notificationOutbox);

    List<NotificationOutbox> findAllByStatus(OutboxStatus status);
}
