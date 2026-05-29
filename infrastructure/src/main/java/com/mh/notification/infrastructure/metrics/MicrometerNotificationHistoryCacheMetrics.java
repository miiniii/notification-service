package com.mh.notification.infrastructure.metrics;

import com.mh.notification.application.port.NotificationHistoryCacheMetrics;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class MicrometerNotificationHistoryCacheMetrics implements NotificationHistoryCacheMetrics {

    public static final String REDIS_HIT = "notification.history.cache.redis.hit";
    public static final String REDIS_MISS = "notification.history.cache.redis.miss";
    public static final String REDIS_ERROR = "notification.history.cache.redis.error";
    public static final String LOCAL_HIT = "notification.history.cache.local.hit";
    public static final String LOCAL_MISS = "notification.history.cache.local.miss";
    public static final String LOCAL_ERROR = "notification.history.cache.local.error";
    public static final String DB_FALLBACK = "notification.history.cache.db.fallback";

    private final Counter redisHit;
    private final Counter redisMiss;
    private final Counter redisError;
    private final Counter localHit;
    private final Counter localMiss;
    private final Counter localError;
    private final Counter dbFallback;

    public MicrometerNotificationHistoryCacheMetrics(MeterRegistry meterRegistry) {
        this.redisHit = Counter.builder(REDIS_HIT).register(meterRegistry);
        this.redisMiss = Counter.builder(REDIS_MISS).register(meterRegistry);
        this.redisError = Counter.builder(REDIS_ERROR).register(meterRegistry);
        this.localHit = Counter.builder(LOCAL_HIT).register(meterRegistry);
        this.localMiss = Counter.builder(LOCAL_MISS).register(meterRegistry);
        this.localError = Counter.builder(LOCAL_ERROR).register(meterRegistry);
        this.dbFallback = Counter.builder(DB_FALLBACK).register(meterRegistry);
    }

    @Override
    public void incrementRedisHit() {
        redisHit.increment();
    }

    @Override
    public void incrementRedisMiss() {
        redisMiss.increment();
    }

    @Override
    public void incrementRedisError() {
        redisError.increment();
    }

    @Override
    public void incrementLocalHit() {
        localHit.increment();
    }

    @Override
    public void incrementLocalMiss() {
        localMiss.increment();
    }

    @Override
    public void incrementLocalError() {
        localError.increment();
    }

    @Override
    public void incrementDbFallback() {
        dbFallback.increment();
    }
}
