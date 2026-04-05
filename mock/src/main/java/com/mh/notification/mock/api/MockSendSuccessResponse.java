package com.mh.notification.mock.api;

public record MockSendSuccessResponse(
        String result,
        String requestId,
        String channelType,
        String processedAt,
        long latencyMs
) {
}
