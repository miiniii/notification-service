package com.mh.notification.application.dto;

import com.mh.notification.domain.Notification;
import com.mh.notification.domain.NotificationChannel;

import java.time.LocalDateTime;

public record NotificationCreateResult(
        Long id,
        Long userId,
        String service,
        NotificationChannel channel,
        String title,
        String body,
        String targetUrl,
        LocalDateTime createdAt
) {
    public static NotificationCreateResult from(Notification notification) {
        return new NotificationCreateResult(
                notification.getId(),
                notification.getUserId(),
                notification.getService(),
                notification.getChannel(),
                notification.getTitle(),
                notification.getBody(),
                notification.getTargetUrl(),
                notification.getCreatedAt()
        );
    }
}
