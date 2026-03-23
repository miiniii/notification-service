package com.mh.notification.infrastructure.persistence;

import com.mh.notification.application.port.NotificationOutboxRepository;
import com.mh.notification.domain.NotificationOutbox;
import com.mh.notification.domain.OutboxStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class NotificationOutboxRepositoryImpl implements NotificationOutboxRepository {

    private final NotificationOutboxJpaRepository notificationOutboxJpaRepository;

    @Override
    public NotificationOutbox save(NotificationOutbox notificationOutbox) {
        return notificationOutboxJpaRepository.save(notificationOutbox);
    }

    @Override
    public List<NotificationOutbox> findAllByStatus(OutboxStatus status) {
        return notificationOutboxJpaRepository.findAllByStatus(status);
    }
}
