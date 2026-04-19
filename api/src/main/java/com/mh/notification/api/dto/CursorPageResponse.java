package com.mh.notification.api.dto;

import java.time.LocalDateTime;
import java.util.List;

public record CursorPageResponse<T>(
        List<T> content,
        boolean hasNext,
        LocalDateTime nextCursorCreatedAt,
        Long nextCursorId
) {
}
