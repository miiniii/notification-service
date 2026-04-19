package com.mh.notification.application.dto;

import java.time.LocalDateTime;
import java.util.List;

public record NotificationCursorResult<T>(
        List<T> content,
        boolean hasNext,
        LocalDateTime nextCursorCreatedAt,
        Long nextCursorId
) {
}