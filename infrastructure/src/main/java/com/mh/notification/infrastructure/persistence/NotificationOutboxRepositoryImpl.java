package com.mh.notification.infrastructure.persistence;

import com.mh.notification.application.port.NotificationOutboxRepository;
import com.mh.notification.domain.NotificationOutbox;
import com.mh.notification.domain.OutboxStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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

    @Override
    public Optional<NotificationOutbox> findById(Long id) {
        return notificationOutboxJpaRepository.findById(id);
    }
}
