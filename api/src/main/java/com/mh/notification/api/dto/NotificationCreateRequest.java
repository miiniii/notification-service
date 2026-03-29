package com.mh.notification.api.dto;

import com.mh.notification.application.dto.NotificationCreateCommand;
import com.mh.notification.domain.NotificationChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record NotificationCreateRequest(
        @NotNull Long userId,
        @NotBlank String service,
        @NotNull NotificationChannel channel,
        @NotBlank String title,
        @NotBlank String body,
        @NotBlank String targetUrl
) {
    public NotificationCreateCommand toCommand() {
        return NotificationCreateCommand.of(
                userId,
                service,
                channel,
                title,
                body,
                targetUrl
        );
    }
}
