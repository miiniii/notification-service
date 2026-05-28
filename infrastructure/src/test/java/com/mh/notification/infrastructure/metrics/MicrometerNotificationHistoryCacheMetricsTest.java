package com.mh.notification.infrastructure.metrics;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MicrometerNotificationHistoryCacheMetricsTest {

    @Test
    void incrementCounters() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        MicrometerNotificationHistoryCacheMetrics metrics =
                new MicrometerNotificationHistoryCacheMetrics(meterRegistry);

        metrics.incrementRedisHit();
        metrics.incrementRedisMiss();
        metrics.incrementRedisError();
        metrics.incrementDbFallback();

        assertThat(meterRegistry.counter(MicrometerNotificationHistoryCacheMetrics.REDIS_HIT).count()).isEqualTo(1);
        assertThat(meterRegistry.counter(MicrometerNotificationHistoryCacheMetrics.REDIS_MISS).count()).isEqualTo(1);
        assertThat(meterRegistry.counter(MicrometerNotificationHistoryCacheMetrics.REDIS_ERROR).count()).isEqualTo(1);
        assertThat(meterRegistry.counter(MicrometerNotificationHistoryCacheMetrics.DB_FALLBACK).count()).isEqualTo(1);
    }
}
