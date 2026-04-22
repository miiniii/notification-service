package com.mh.notification.application.dto;

import com.mh.notification.domain.Notification;

import java.time.LocalDateTime;

public record ArchiveNotificationRow(
        Long id,
        Long requesterId,
        Long userId,
        String requestId,
        String service,
        String title,
        String body,
        String targetUrl,
        boolean isRead,
        String channel,
        LocalDateTime createdAt
) {
    public static ArchiveNotificationRow from(Notification notification) {
        return new ArchiveNotificationRow(
                notification.getId(),
                notification.getRequesterId(),
                notification.getUserId(),
                notification.getRequestId(),
                notification.getService(),
                notification.getTitle(),
                notification.getBody(),
                notification.getTargetUrl(),
                notification.isRead(),
                notification.getChannel().name(),
                notification.getCreatedAt()
        );
    }
}
