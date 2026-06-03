package com.mh.notification.application.service;

import com.mh.notification.application.dto.NotificationCursorResult;
import com.mh.notification.application.dto.NotificationHistoryCacheKey;
import com.mh.notification.application.dto.NotificationHistoryCacheLookup;
import com.mh.notification.application.dto.NotificationHistoryQueryResult;
import com.mh.notification.application.port.NotificationHistoryCacheMetrics;
import com.mh.notification.application.port.NotificationHistoryCacheRepository;
import com.mh.notification.application.port.NotificationHistoryLocalCacheRepository;
import com.mh.notification.application.usecase.NotificationCursorQueryUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CachedNotificationCursorQueryService implements NotificationCursorQueryUseCase {

    private final NotificationHistoryLocalCacheRepository notificationHistoryLocalCacheRepository;
    private final NotificationHistoryCacheRepository notificationHistoryCacheRepository;
    private final NotificationHistoryCacheMetrics notificationHistoryCacheMetrics;
    private final NotificationCursorQueryService notificationCursorQueryService;

    @Override
    public NotificationCursorResult<NotificationHistoryQueryResult> getRecentNotifications(
            Long requesterId,
            LocalDateTime cursorCreatedAt,
            Long cursorId,
            int size
    ) {
        NotificationHistoryCacheKey key = new NotificationHistoryCacheKey(
                requesterId,
                cursorCreatedAt,
                cursorId,
                size
        );

        NotificationHistoryCacheLookup localLookup = notificationHistoryLocalCacheRepository.get(key);
        switch (localLookup.status()) {
            case HIT -> {
                notificationHistoryCacheMetrics.incrementLocalHit();
                return localLookup.value();
            }
            case MISS -> notificationHistoryCacheMetrics.incrementLocalMiss();
            case ERROR -> notificationHistoryCacheMetrics.incrementLocalError();
        }

        NotificationHistoryCacheLookup lookup = notificationHistoryCacheRepository.get(key);

        switch (lookup.status()) {
            case HIT -> {
                notificationHistoryCacheMetrics.incrementRedisHit();
                putLocalCache(key, lookup.value());
                return lookup.value();
            }
            case MISS -> notificationHistoryCacheMetrics.incrementRedisMiss();
            case ERROR -> notificationHistoryCacheMetrics.incrementRedisError();
        }

        notificationHistoryCacheMetrics.incrementDbFallback();

        NotificationCursorResult<NotificationHistoryQueryResult> result =
                notificationCursorQueryService.getRecentNotifications(
                        requesterId,
                        cursorCreatedAt,
                        cursorId,
                        size
                );

        boolean stored = notificationHistoryCacheRepository.put(key, result);
        if (!stored) {
            notificationHistoryCacheMetrics.incrementRedisError();
        }

        putLocalCache(key, result);

        return result;
    }

    private void putLocalCache(
            NotificationHistoryCacheKey key,
            NotificationCursorResult<NotificationHistoryQueryResult> result
    ) {
        boolean stored = notificationHistoryLocalCacheRepository.put(key, result);
        if (!stored) {
            notificationHistoryCacheMetrics.incrementLocalError();
        }
    }
}
