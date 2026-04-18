package com.mh.notification.application.dto;

import com.mh.notification.domain.Notification;
import com.mh.notification.domain.NotificationChannel;

public record NotificationOutboxPayload(
        Long notificationId,
        String requestId,
        Long requesterId,
        Long userId,
        String service,
        NotificationChannel channel,
        String title,
        String body,
        String targetUrl,
        String receiver
) {
    public static NotificationOutboxPayload from(Notification notification, String receiver) {
        return new NotificationOutboxPayload(
                notification.getId(),
                notification.getRequestId(),
                notification.getRequesterId(),
                notification.getUserId(),
                notification.getService(),
                notification.getChannel(),
                notification.getTitle(),
                notification.getBody(),
                notification.getTargetUrl(),
                receiver
        );
    }
}
