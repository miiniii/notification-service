package com.mh.notification.application.dto;

import com.mh.notification.domain.Notification;
import com.mh.notification.domain.NotificationChannel;

import java.time.LocalDateTime;
import java.util.List;

public record NotificationHistoryQueryResult(
        Long notificationId,
        String requestId,
        Long requesterId,
        Long userId,
        String service,
        NotificationChannel channel,
        String title,
        String body,
        String targetUrl,
        boolean isRead,
        LocalDateTime createdAt,
        List<NotificationSendResultQueryResult> sendResults
) {
    public static NotificationHistoryQueryResult of(
            Notification notification,
            List<NotificationSendResultQueryResult> sendResults
    ) {
        return new NotificationHistoryQueryResult(
                notification.getId(),
                notification.getRequestId(),
                notification.getRequesterId(),
                notification.getUserId(),
                notification.getService(),
                notification.getChannel(),
                notification.getTitle(),
                notification.getBody(),
                notification.getTargetUrl(),
                notification.isRead(),
                notification.getCreatedAt(),
                sendResults
        );
    }
}
