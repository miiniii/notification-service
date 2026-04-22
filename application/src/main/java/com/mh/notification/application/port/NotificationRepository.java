package com.mh.notification.application.port;

import com.mh.notification.domain.Notification;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepository {

    Notification save(Notification notification);

    List<Notification> findRecentByRequesterIdWithCursor(
            Long requesterId,
            LocalDateTime from,
            LocalDateTime cursorCreatedAt,
            Long cursorId,
            int size
    );

    List<Notification> findOldNotifications(LocalDateTime cutoff, int limit);

    void deleteAllByIds(List<Long> ids);
}
