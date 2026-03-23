package com.mh.notification.application.dto;

import com.mh.notification.domain.Notification;
import com.mh.notification.domain.NotificationChannel;

public record NotificationOutboxPayload(
        Long notificationId,
        Long userId,
        String service,
        NotificationChannel channel,
        String title,
        String body,
        String targetUrl
) {
    public static NotificationOutboxPayload from(Notification notification) {
        return new NotificationOutboxPayload(
                notification.getId(),
                notification.getUserId(),
                notification.getService(),
                notification.getChannel(),
                notification.getTitle(),
                notification.getBody(),
                notification.getTargetUrl()
        );
    }
}
