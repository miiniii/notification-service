package com.mh.notification.api.dto;

import com.mh.notification.application.dto.NotificationCreateResult;
import com.mh.notification.domain.NotificationChannel;

import java.time.LocalDateTime;

public record NotificationCreateResponse(
        Long id,
        Long userId,
        String service,
        NotificationChannel channel,
        String title,
        String body,
        String targetUrl,
        LocalDateTime createdAt
) {
    public static NotificationCreateResponse from(NotificationCreateResult result) {
        return new NotificationCreateResponse(
                result.id(),
                result.userId(),
                result.service(),
                result.channel(),
                result.title(),
                result.body(),
                result.targetUrl(),
                result.createdAt()
        );
    }
}
