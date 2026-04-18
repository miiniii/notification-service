package com.mh.notification.application.dto;

import com.mh.notification.domain.NotificationChannel;

public record NotificationCreateCommand(
        Long requesterId,
        Long userId,
        String service,
        NotificationChannel channel,
        String title,
        String body,
        String targetUrl,
        String receiver
) {
    public static NotificationCreateCommand of(Long requesterId,
                                               Long userId,
                                               String service,
                                               NotificationChannel channel,
                                               String title,
                                               String body,
                                               String targetUrl,
                                               String receiver) {
        return new NotificationCreateCommand(
                requesterId,
                userId,
                service,
                channel,
                title,
                body,
                targetUrl,
                receiver
        );
    }
}
