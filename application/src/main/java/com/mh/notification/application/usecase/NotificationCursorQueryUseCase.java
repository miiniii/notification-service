package com.mh.notification.application.usecase;

import com.mh.notification.application.dto.NotificationCursorResult;
import com.mh.notification.application.dto.NotificationHistoryQueryResult;

import java.time.LocalDateTime;

public interface NotificationCursorQueryUseCase {

    NotificationCursorResult<NotificationHistoryQueryResult> getRecentNotifications(
            Long requesterId,
            LocalDateTime cursorCreatedAt,
            Long cursorId,
            int size
    );
}
