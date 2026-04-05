package com.mh.notification.application.port;

import com.mh.notification.domain.NotificationOutbox;
import com.mh.notification.domain.OutboxStatus;

import java.util.List;
import java.util.Optional;

public interface NotificationOutboxRepository {

    NotificationOutbox save(NotificationOutbox notificationOutbox);

    List<NotificationOutbox> findAllByStatus(OutboxStatus status);

    Optional<NotificationOutbox> findById(Long id);
}
