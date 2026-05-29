package com.mh.notification.infrastructure.cache;

import com.github.benmanes.caffeine.cache.Ticker;
import com.mh.notification.application.dto.NotificationCursorResult;
import com.mh.notification.application.dto.NotificationHistoryCacheKey;
import com.mh.notification.application.dto.NotificationHistoryCacheLookup;
import com.mh.notification.application.dto.NotificationHistoryCacheLookupStatus;
import com.mh.notification.application.dto.NotificationHistoryQueryResult;
import com.mh.notification.domain.NotificationChannel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

class CaffeineNotificationHistoryLocalCacheRepositoryTest {

    private AtomicLong nanos;
    private CaffeineNotificationHistoryLocalCacheRepository repository;

    @BeforeEach
    void setUp() {
        nanos = new AtomicLong();

        NotificationHistoryLocalCacheProperties properties = new NotificationHistoryLocalCacheProperties();
        properties.setTtlSeconds(10);
        properties.setMaximumSize(10000);

        Ticker ticker = nanos::get;
        repository = new CaffeineNotificationHistoryLocalCacheRepository(properties, ticker);
    }

    @Test
    void get_whenValueExists_thenReturnHit() {
        NotificationCursorResult<NotificationHistoryQueryResult> expected = result();
        boolean stored = repository.put(cacheKey(), expected);

        NotificationHistoryCacheLookup lookup = repository.get(cacheKey());

        assertThat(stored).isTrue();
        assertThat(lookup.status()).isEqualTo(NotificationHistoryCacheLookupStatus.HIT);
        assertThat(lookup.value()).isEqualTo(expected);
    }

    @Test
    void get_whenValueMissing_thenReturnMiss() {
        NotificationHistoryCacheLookup lookup = repository.get(cacheKey());

        assertThat(lookup.status()).isEqualTo(NotificationHistoryCacheLookupStatus.MISS);
        assertThat(lookup.value()).isNull();
    }

    @Test
    void get_whenTtlExpired_thenReturnMiss() {
        repository.put(cacheKey(), result());

        nanos.addAndGet(TimeUnit.SECONDS.toNanos(11));

        NotificationHistoryCacheLookup lookup = repository.get(cacheKey());

        assertThat(lookup.status()).isEqualTo(NotificationHistoryCacheLookupStatus.MISS);
        assertThat(lookup.value()).isNull();
    }

    private NotificationHistoryCacheKey cacheKey() {
        return new NotificationHistoryCacheKey(
                1L,
                LocalDateTime.of(2026, 5, 28, 10, 0),
                10L,
                20
        );
    }

    private NotificationCursorResult<NotificationHistoryQueryResult> result() {
        NotificationHistoryQueryResult item = new NotificationHistoryQueryResult(
                10L,
                "req-123",
                1L,
                2L,
                "PAYMENT",
                NotificationChannel.EMAIL,
                "title",
                "body",
                "/target",
                false,
                LocalDateTime.of(2026, 5, 28, 9, 0),
                List.of()
        );

        return new NotificationCursorResult<>(
                List.of(item),
                false,
                item.createdAt(),
                item.notificationId()
        );
    }
}
