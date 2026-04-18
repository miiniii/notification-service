package com.mh.notification.application.dto;

import com.mh.notification.domain.NotificationChannel;

import java.time.LocalDateTime;

/**
 * Consumer -> sender
 */
public record NotificationMessage(
        Long notificationId,
        String requestId,
        Long requesterId,
        Long userId,
        String service,
        NotificationChannel channel,
        String title,
        String body,
        String targetUrl,
        String receiver,
        int retryCount,
        LocalDateTime nextRetryAt
) {
    public static NotificationMessage initial(
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
        return new NotificationMessage(
                notificationId,
                requestId,
                requesterId,
                userId,
                service,
                channel,
                title,
                body,
                targetUrl,
                receiver,
                0,
                null
        );
    }

    public NotificationMessage incrementRetry(LocalDateTime nextRetryAt) {
        return new NotificationMessage(
                notificationId,
                requestId,
                requesterId,
                userId,
                service,
                channel,
                title,
                body,
                targetUrl,
                receiver,
                retryCount + 1,
                nextRetryAt
        );
    }
}