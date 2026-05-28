package com.mh.notification.application.service;

import com.mh.notification.application.dto.NotificationCursorResult;
import com.mh.notification.application.dto.NotificationHistoryCacheKey;
import com.mh.notification.application.dto.NotificationHistoryCacheLookup;
import com.mh.notification.application.dto.NotificationHistoryQueryResult;
import com.mh.notification.application.port.NotificationHistoryCacheMetrics;
import com.mh.notification.application.port.NotificationHistoryCacheRepository;
import com.mh.notification.application.usecase.NotificationCursorQueryUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CachedNotificationCursorQueryService implements NotificationCursorQueryUseCase {

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

        NotificationHistoryCacheLookup lookup = notificationHistoryCacheRepository.get(key);

        switch (lookup.status()) {
            case HIT -> {
                notificationHistoryCacheMetrics.incrementRedisHit();
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

        return result;
    }
}
