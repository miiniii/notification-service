package com.mh.notification.infrastructure.client.mock.dto;

import java.util.Map;

public record MockApiSendRequest(
        String requestId,
        String channelType,
        String receiver,
        String message,
        Map<String, Object> metadata
) {
}