package com.mh.notification.infrastructure.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Ticker;
import com.mh.notification.application.dto.NotificationCursorResult;
import com.mh.notification.application.dto.NotificationHistoryCacheKey;
import com.mh.notification.application.dto.NotificationHistoryCacheLookup;
import com.mh.notification.application.dto.NotificationHistoryQueryResult;
import com.mh.notification.application.port.NotificationHistoryLocalCacheRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Slf4j
@Repository
public class CaffeineNotificationHistoryLocalCacheRepository implements NotificationHistoryLocalCacheRepository {

    private final Cache<NotificationHistoryCacheKey, NotificationCursorResult<NotificationHistoryQueryResult>> cache;

    @Autowired
    public CaffeineNotificationHistoryLocalCacheRepository(NotificationHistoryLocalCacheProperties properties) {
        this(properties, Ticker.systemTicker());
    }

    CaffeineNotificationHistoryLocalCacheRepository(NotificationHistoryLocalCacheProperties properties, Ticker ticker) {
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofSeconds(properties.getTtlSeconds()))
                .maximumSize(properties.getMaximumSize())
                .ticker(ticker)
                .build();
    }

    @Override
    public NotificationHistoryCacheLookup get(NotificationHistoryCacheKey key) {
        try {
            NotificationCursorResult<NotificationHistoryQueryResult> value = cache.getIfPresent(key);
            if (value == null) {
                return NotificationHistoryCacheLookup.miss();
            }

            return NotificationHistoryCacheLookup.hit(value);
        } catch (Exception e) {
            log.warn("[LOCAL CACHE GET FAIL] notification history local cache get failed. key={}", key, e);
            return NotificationHistoryCacheLookup.error();
        }
    }

    @Override
    public boolean put(
            NotificationHistoryCacheKey key,
            NotificationCursorResult<NotificationHistoryQueryResult> value
    ) {
        try {
            cache.put(key, value);
            return true;
        } catch (Exception e) {
            log.warn("[LOCAL CACHE PUT FAIL] notification history local cache put failed. key={}", key, e);
            return false;
        }
    }
}
