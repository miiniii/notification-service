package com.mh.notification.infrastructure.redis;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.cache.notification-history")
public class NotificationHistoryCacheProperties {

    private long ttlSeconds = 60;
}
