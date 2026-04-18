package com.mh.notification.infrastructure.sender;

import com.mh.notification.application.dto.NotificationMessage;
import com.mh.notification.application.sender.NotificationSender;
import com.mh.notification.infrastructure.client.mock.dto.MockApiSendRequest;
import com.mh.notification.infrastructure.gateway.NotificationSendGateway;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public abstract class AbstractMockNotificationSender implements NotificationSender {

    private final NotificationSendGateway notificationSendGateway;

    protected AbstractMockNotificationSender(NotificationSendGateway notificationSendGateway) {
        this.notificationSendGateway = notificationSendGateway;
    }

    @Override
    public void send(NotificationMessage message) {
        MockApiSendRequest request = new MockApiSendRequest(
                message.requestId(),
                resolveChannelType(message),
                resolveReceiver(message),
                buildMessage(message),
                buildMetadata(message)
        );

        log.info("[SENDER] traceId={}", message.requestId());
        notificationSendGateway.send(request, message.requestId());
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
