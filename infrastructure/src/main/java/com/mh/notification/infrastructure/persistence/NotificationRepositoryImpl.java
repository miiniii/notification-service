package com.mh.notification.infrastructure.persistence;

import com.mh.notification.application.port.NotificationRepository;
import com.mh.notification.domain.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepository {

    private final NotificationJpaRepository notificationJpaRepository;

    @Override
    public Notification save(Notification notification) {
        return notificationJpaRepository.save(notification);
    }

    @Override
    public Page<Notification> findRecentByRequesterId(Long requesterId, LocalDateTime from, Pageable pageable) {
        return notificationJpaRepository.findByRequesterIdAndCreatedAtGreaterThanEqual(
                requesterId,
                from,
                pageable
        );
    }

    @Override
    public List<Notification> findByIds(List<Long> ids) {
        return notificationJpaRepository.findByIdIn(ids);
    }
}
