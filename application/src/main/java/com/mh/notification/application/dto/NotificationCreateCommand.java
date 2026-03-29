package com.mh.notification.application.dto;

import com.mh.notification.domain.NotificationChannel;

public record NotificationCreateCommand(
        Long userId,
        String service,
        NotificationChannel channel,
        String title,
        String body,
        String targetUrl
) {
    public static NotificationCreateCommand of(Long userId,
                                               String service,
                                               NotificationChannel channel,
                                               String title,
                                               String body,
                                               String targetUrl) {
        return new NotificationCreateCommand(
                userId,
                service,
                channel,
                title,
                body,
                targetUrl
        );
    }
}
