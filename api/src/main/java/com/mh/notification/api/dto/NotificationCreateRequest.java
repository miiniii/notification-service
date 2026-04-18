package com.mh.notification.api.dto;

import com.mh.notification.application.dto.NotificationCreateCommand;
import com.mh.notification.domain.NotificationChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record NotificationCreateRequest(
        @NotNull Long requesterId,
        @NotNull Long userId,
        @NotBlank String service,
        @NotNull NotificationChannel channel,
        @NotBlank String title,
        @NotBlank String body,
        @NotBlank String targetUrl,
        @NotBlank String receiver
) {
    public NotificationCreateCommand toCommand() {
        return NotificationCreateCommand.of(
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
