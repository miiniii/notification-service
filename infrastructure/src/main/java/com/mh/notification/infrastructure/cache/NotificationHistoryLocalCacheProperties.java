package com.mh.notification.infrastructure.cache;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.cache.notification-history.local")
public class NotificationHistoryLocalCacheProperties {

    private long ttlSeconds = 10;
    private long maximumSize = 10000;
}
