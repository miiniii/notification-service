package com.mh.notification.application.dto;

public record ConsumeTaskResult(
        String recordId,
        NotificationMessage message,
        ConsumeResult result,
        long deliveryCount
) {
}
