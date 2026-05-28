package com.mh.notification.application.port;

public interface NotificationHistoryCacheMetrics {

    void incrementRedisHit();

    void incrementRedisMiss();

    void incrementRedisError();

    void incrementDbFallback();
}
