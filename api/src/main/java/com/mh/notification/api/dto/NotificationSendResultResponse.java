package com.mh.notification.api.dto;

import com.mh.notification.application.dto.NotificationSendResultQueryResult;
import com.mh.notification.domain.FailureType;
import com.mh.notification.domain.NotificationChannel;
import com.mh.notification.domain.NotificationSendResult;
import com.mh.notification.domain.SendStatus;

import java.time.LocalDateTime;

public record NotificationSendResultResponse(
        Long id,
        NotificationChannel channel,
        SendStatus status,
        String failureReason,
        FailureType failureType,
        Integer failureStatusCode,
        int retryCount,
        LocalDateTime processedAt
) {
    public static NotificationSendResultResponse from(NotificationSendResultQueryResult result) {
        return new NotificationSendResultResponse(
                result.id(),
                result.channel(),
                result.status(),
                result.failureReason(),
                result.failureType(),
                result.failureStatusCode(),
                result.retryCount(),
                result.processedAt()
        );
    }
}
