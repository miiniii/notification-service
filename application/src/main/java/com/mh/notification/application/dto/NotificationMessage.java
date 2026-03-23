package com.mh.notification.application.dto;

import com.mh.notification.domain.NotificationChannel;

import java.time.LocalDateTime;

/**
 * Consumer -> sender
 */
public record NotificationMessage(
        Long notificationId,
        Long userId,
        String service,
        NotificationChannel channel,
        String title,
        String body,
        String targetUrl,
        int retryCount,
        LocalDateTime nextRetryAt
) {
    public static NotificationMessage initial(
            Long notificationId,
            Long userId,
            String service,
            NotificationChannel channel,
            String title,
            String body,
            String targetUrl
    ) {
        return new NotificationMessage(
                notificationId,
                userId,
                service,
                channel,
                title,
                body,
                targetUrl,
                0,
                null
        );
    }

    public NotificationMessage incrementRetry(LocalDateTime nextRetryAt) {
        return new NotificationMessage(
                notificationId,
                userId,
                service,
                channel,
                title,
                body,
                targetUrl,
                retryCount + 1,
                nextRetryAt
        );
    }
}