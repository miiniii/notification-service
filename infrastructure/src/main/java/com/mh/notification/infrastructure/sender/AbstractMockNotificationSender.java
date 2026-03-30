package com.mh.notification.infrastructure.sender;

import com.mh.notification.application.dto.NotificationMessage;
import com.mh.notification.application.sender.NotificationSender;
import com.mh.notification.infrastructure.client.mock.MockApiClient;
import com.mh.notification.infrastructure.client.mock.dto.MockSendRequest;

import java.util.Map;
import java.util.UUID;

public abstract class AbstractMockNotificationSender implements NotificationSender {

    private final MockApiClient mockApiClient;

    protected AbstractMockNotificationSender(MockApiClient mockApiClient) {
        this.mockApiClient = mockApiClient;
    }

    @Override
    public void send(NotificationMessage message) {
        MockSendRequest request = new MockSendRequest(
                createRequestId(message),
                resolveChannelType(message),
                resolveReceiver(message),
                buildMessage(message),
                buildMetadata(message)
        );

        mockApiClient.send(request);
    }

    protected String createRequestId(NotificationMessage message) {
        String shortUuid = UUID.randomUUID().toString().substring(0, 8);
        return "n" + message.notificationId() + "-r" + message.retryCount() + "-" + shortUuid;
    }

    protected String buildMessage(NotificationMessage message) {
        String title = safe(message.title());
        String body = safe(message.body());

        if (!title.isBlank()) {
            return title + "\n" + body;
        }
        return body;
    }

    protected Map<String, Object> buildMetadata(NotificationMessage message) {
        return Map.of(
                "notificationId", message.notificationId(),
                "channel", message.channel().name(),
                "retryCount", message.retryCount(),
                "title", safe(message.title())
        );
    }

    protected String safe(String value) {
        return value == null ? "" : value;
    }

    protected abstract String resolveChannelType(NotificationMessage message);

    protected abstract String resolveReceiver(NotificationMessage message);
}
