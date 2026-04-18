package com.mh.notification.application.dto;

import com.mh.notification.domain.FailureType;
import com.mh.notification.domain.NotificationChannel;
import com.mh.notification.domain.NotificationSendResult;
import com.mh.notification.domain.SendStatus;

import java.time.LocalDateTime;

public record NotificationSendResultQueryResult(
        Long id,
        NotificationChannel channel,
        SendStatus status,
        String failureReason,
        FailureType failureType,
        Integer failureStatusCode,
        int retryCount,
        LocalDateTime processedAt
) {
    public static NotificationSendResultQueryResult from(NotificationSendResult result) {
        return new NotificationSendResultQueryResult(
                result.getId(),
                result.getChannel(),
                result.getStatus(),
                result.getFailureReason(),
                result.getFailureType(),
                result.getFailureStatusCode(),
                result.getRetryCount(),
                result.getProcessedAt()
        );
    }
}
