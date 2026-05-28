package com.mh.notification.application.dto;

import java.time.LocalDateTime;

public record NotificationHistoryCacheKey(
        Long requesterId,
        LocalDateTime cursorCreatedAt,
        Long cursorId,
        int size
) {
}
