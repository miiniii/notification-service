package com.mh.notification.api.dto;

import com.mh.notification.application.dto.NotificationHistoryQueryResult;
import com.mh.notification.domain.NotificationChannel;

import java.time.LocalDateTime;
import java.util.List;

public record NotificationHistoryResponse(
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
        List<NotificationSendResultResponse> sendResults
) {
    public static NotificationHistoryResponse from(NotificationHistoryQueryResult result) {
        return new NotificationHistoryResponse(
                result.notificationId(),
                result.requestId(),
                result.requesterId(),
                result.userId(),
                result.service(),
                result.channel(),
                result.title(),
                result.body(),
                result.targetUrl(),
                result.isRead(),
                result.createdAt(),
                result.sendResults().stream()
                        .map(NotificationSendResultResponse::from)
                        .toList()
        );
    }
}
