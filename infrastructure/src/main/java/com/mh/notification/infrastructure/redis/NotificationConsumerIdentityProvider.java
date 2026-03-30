package com.mh.notification.infrastructure.redis;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class NotificationConsumerIdentityProvider {

    private final String consumerName = "notification-consumer-" + UUID.randomUUID();

    public String getConsumerName() {
        return consumerName;
    }
}
