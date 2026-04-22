package com.mh.notification.infrastructure.persistence;

import com.mh.notification.application.port.NotificationRepository;
import com.mh.notification.domain.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
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
    public List<Notification> findRecentByRequesterIdWithCursor(Long requesterId, LocalDateTime from, LocalDateTime cursorCreatedAt, Long cursorId, int size) {
        return notificationJpaRepository.findRecentByRequesterIdWithCursor(
                requesterId,
                from,
                cursorCreatedAt,
                cursorId,
                PageRequest.of(0, size)
        );
    }

    @Override
    public List<Notification> findOldNotifications(LocalDateTime cutoff, int limit) {
        return notificationJpaRepository.findTop1000ByCreatedAtBeforeOrderByCreatedAtAsc(cutoff);
    }

    @Override
    public void deleteAllByIds(List<Long> ids) {
        notificationJpaRepository.deleteAllByIdIn(ids);
    }
}
