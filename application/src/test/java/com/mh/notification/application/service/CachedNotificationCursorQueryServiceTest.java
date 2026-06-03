package com.mh.notification.application.service;

import com.mh.notification.application.dto.NotificationCursorResult;
import com.mh.notification.application.dto.NotificationHistoryCacheKey;
import com.mh.notification.application.dto.NotificationHistoryCacheLookup;
import com.mh.notification.application.dto.NotificationHistoryQueryResult;
import com.mh.notification.application.port.NotificationHistoryCacheMetrics;
import com.mh.notification.application.port.NotificationHistoryCacheRepository;
import com.mh.notification.application.port.NotificationHistoryLocalCacheRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CachedNotificationCursorQueryServiceTest {

    @Mock
    private NotificationHistoryLocalCacheRepository notificationHistoryLocalCacheRepository;

    @Mock
    private NotificationHistoryCacheRepository notificationHistoryCacheRepository;

    @Mock
    private NotificationHistoryCacheMetrics notificationHistoryCacheMetrics;

    @Mock
    private NotificationCursorQueryService notificationCursorQueryService;

    private CachedNotificationCursorQueryService cachedNotificationCursorQueryService;

    @BeforeEach
    void setUp() {
        cachedNotificationCursorQueryService = new CachedNotificationCursorQueryService(
                notificationHistoryLocalCacheRepository,
                notificationHistoryCacheRepository,
                notificationHistoryCacheMetrics,
                notificationCursorQueryService
        );
    }

    @Test
    void getRecentNotifications_whenLocalHit_thenReturnLocalCacheAndDoNotCallRedisOrDb() {
        NotificationCursorResult<NotificationHistoryQueryResult> cachedResult = emptyResult();
        given(notificationHistoryLocalCacheRepository.get(any(NotificationHistoryCacheKey.class)))
                .willReturn(NotificationHistoryCacheLookup.hit(cachedResult));

        NotificationCursorResult<NotificationHistoryQueryResult> result =
                cachedNotificationCursorQueryService.getRecentNotifications(1L, null, null, 20);

        assertThat(result).isSameAs(cachedResult);
        verify(notificationHistoryCacheMetrics).incrementLocalHit();
        verify(notificationHistoryCacheRepository, never()).get(any());
        verify(notificationCursorQueryService, never()).getRecentNotifications(anyLong(), any(), any(), anyInt());
        verify(notificationHistoryCacheRepository, never()).put(any(), any());
        verify(notificationHistoryLocalCacheRepository, never()).put(any(), any());
    }

    @Test
    void getRecentNotifications_whenLocalMissAndRedisHit_thenPutLocalAndReturnRedisCache() {
        NotificationCursorResult<NotificationHistoryQueryResult> cachedResult = emptyResult();
        given(notificationHistoryLocalCacheRepository.get(any(NotificationHistoryCacheKey.class)))
                .willReturn(NotificationHistoryCacheLookup.miss());
        given(notificationHistoryCacheRepository.get(any(NotificationHistoryCacheKey.class)))
                .willReturn(NotificationHistoryCacheLookup.hit(cachedResult));
        given(notificationHistoryLocalCacheRepository.put(any(NotificationHistoryCacheKey.class), eq(cachedResult)))
                .willReturn(true);

        NotificationCursorResult<NotificationHistoryQueryResult> result =
                cachedNotificationCursorQueryService.getRecentNotifications(1L, null, null, 20);

        assertThat(result).isSameAs(cachedResult);
        verify(notificationHistoryCacheMetrics).incrementLocalMiss();
        verify(notificationHistoryCacheMetrics).incrementRedisHit();
        verify(notificationHistoryLocalCacheRepository).put(any(NotificationHistoryCacheKey.class), eq(cachedResult));
        verify(notificationCursorQueryService, never()).getRecentNotifications(anyLong(), any(), any(), anyInt());
        verify(notificationHistoryCacheRepository, never()).put(any(), any());
    }

    @Test
    void getRecentNotifications_whenLocalMissAndRedisMiss_thenCallDbAndStoreRedisAndLocal() {
        NotificationCursorResult<NotificationHistoryQueryResult> dbResult = emptyResult();
        LocalDateTime cursorCreatedAt = LocalDateTime.of(2026, 5, 28, 10, 0);
        given(notificationHistoryLocalCacheRepository.get(any(NotificationHistoryCacheKey.class)))
                .willReturn(NotificationHistoryCacheLookup.miss());
        given(notificationHistoryCacheRepository.get(any(NotificationHistoryCacheKey.class)))
                .willReturn(NotificationHistoryCacheLookup.miss());
        given(notificationCursorQueryService.getRecentNotifications(1L, cursorCreatedAt, 10L, 20))
                .willReturn(dbResult);
        given(notificationHistoryCacheRepository.put(any(NotificationHistoryCacheKey.class), eq(dbResult)))
                .willReturn(true);
        given(notificationHistoryLocalCacheRepository.put(any(NotificationHistoryCacheKey.class), eq(dbResult)))
                .willReturn(true);

        NotificationCursorResult<NotificationHistoryQueryResult> result =
                cachedNotificationCursorQueryService.getRecentNotifications(1L, cursorCreatedAt, 10L, 20);

        assertThat(result).isSameAs(dbResult);
        verify(notificationHistoryCacheMetrics).incrementLocalMiss();
        verify(notificationHistoryCacheMetrics).incrementRedisMiss();
        verify(notificationHistoryCacheMetrics).incrementDbFallback();
        verify(notificationCursorQueryService).getRecentNotifications(1L, cursorCreatedAt, 10L, 20);

        ArgumentCaptor<NotificationHistoryCacheKey> keyCaptor =
                ArgumentCaptor.forClass(NotificationHistoryCacheKey.class);
        verify(notificationHistoryCacheRepository).put(keyCaptor.capture(), eq(dbResult));
        verify(notificationHistoryLocalCacheRepository).put(eq(keyCaptor.getValue()), eq(dbResult));
        assertThat(keyCaptor.getValue()).isEqualTo(new NotificationHistoryCacheKey(1L, cursorCreatedAt, 10L, 20));
    }

    @Test
    void getRecentNotifications_whenLocalGetFails_thenFallbackToRedis() {
        NotificationCursorResult<NotificationHistoryQueryResult> cachedResult = emptyResult();
        given(notificationHistoryLocalCacheRepository.get(any(NotificationHistoryCacheKey.class)))
                .willReturn(NotificationHistoryCacheLookup.error());
        given(notificationHistoryCacheRepository.get(any(NotificationHistoryCacheKey.class)))
                .willReturn(NotificationHistoryCacheLookup.hit(cachedResult));
        given(notificationHistoryLocalCacheRepository.put(any(NotificationHistoryCacheKey.class), eq(cachedResult)))
                .willReturn(true);

        NotificationCursorResult<NotificationHistoryQueryResult> result =
                cachedNotificationCursorQueryService.getRecentNotifications(1L, null, null, 20);

        assertThat(result).isSameAs(cachedResult);
        verify(notificationHistoryCacheMetrics).incrementLocalError();
        verify(notificationHistoryCacheMetrics).incrementRedisHit();
        verify(notificationHistoryCacheRepository).get(any(NotificationHistoryCacheKey.class));
    }

    @Test
    void getRecentNotifications_whenLocalPutFailsAfterRedisHit_thenReturnRedisCache() {
        NotificationCursorResult<NotificationHistoryQueryResult> cachedResult = emptyResult();
        given(notificationHistoryLocalCacheRepository.get(any(NotificationHistoryCacheKey.class)))
                .willReturn(NotificationHistoryCacheLookup.miss());
        given(notificationHistoryCacheRepository.get(any(NotificationHistoryCacheKey.class)))
                .willReturn(NotificationHistoryCacheLookup.hit(cachedResult));
        given(notificationHistoryLocalCacheRepository.put(any(NotificationHistoryCacheKey.class), eq(cachedResult)))
                .willReturn(false);

        NotificationCursorResult<NotificationHistoryQueryResult> result =
                cachedNotificationCursorQueryService.getRecentNotifications(1L, null, null, 20);

        assertThat(result).isSameAs(cachedResult);
        verify(notificationHistoryCacheMetrics).incrementLocalMiss();
        verify(notificationHistoryCacheMetrics).incrementRedisHit();
        verify(notificationHistoryCacheMetrics).incrementLocalError();
    }

    @Test
    void getRecentNotifications_whenRedisGetFails_thenFallbackToDbAndPutLocal() {
        NotificationCursorResult<NotificationHistoryQueryResult> dbResult = emptyResult();
        given(notificationHistoryLocalCacheRepository.get(any(NotificationHistoryCacheKey.class)))
                .willReturn(NotificationHistoryCacheLookup.miss());
        given(notificationHistoryCacheRepository.get(any(NotificationHistoryCacheKey.class)))
                .willReturn(NotificationHistoryCacheLookup.error());
        given(notificationCursorQueryService.getRecentNotifications(1L, null, null, 20))
                .willReturn(dbResult);
        given(notificationHistoryCacheRepository.put(any(NotificationHistoryCacheKey.class), eq(dbResult)))
                .willReturn(true);
        given(notificationHistoryLocalCacheRepository.put(any(NotificationHistoryCacheKey.class), eq(dbResult)))
                .willReturn(true);

        NotificationCursorResult<NotificationHistoryQueryResult> result =
                cachedNotificationCursorQueryService.getRecentNotifications(1L, null, null, 20);

        assertThat(result).isSameAs(dbResult);
        verify(notificationHistoryCacheMetrics).incrementLocalMiss();
        verify(notificationHistoryCacheMetrics).incrementRedisError();
        verify(notificationHistoryCacheMetrics).incrementDbFallback();
        verify(notificationHistoryLocalCacheRepository).put(any(NotificationHistoryCacheKey.class), eq(dbResult));
    }

    @Test
    void getRecentNotifications_whenRedisPutFails_thenReturnDbResultAndPutLocal() {
        NotificationCursorResult<NotificationHistoryQueryResult> dbResult = emptyResult();
        given(notificationHistoryLocalCacheRepository.get(any(NotificationHistoryCacheKey.class)))
                .willReturn(NotificationHistoryCacheLookup.miss());
        given(notificationHistoryCacheRepository.get(any(NotificationHistoryCacheKey.class)))
                .willReturn(NotificationHistoryCacheLookup.miss());
        given(notificationCursorQueryService.getRecentNotifications(1L, null, null, 20))
                .willReturn(dbResult);
        given(notificationHistoryCacheRepository.put(any(NotificationHistoryCacheKey.class), eq(dbResult)))
                .willReturn(false);
        given(notificationHistoryLocalCacheRepository.put(any(NotificationHistoryCacheKey.class), eq(dbResult)))
                .willReturn(true);

        NotificationCursorResult<NotificationHistoryQueryResult> result =
                cachedNotificationCursorQueryService.getRecentNotifications(1L, null, null, 20);

        assertThat(result).isSameAs(dbResult);
        verify(notificationHistoryCacheMetrics).incrementLocalMiss();
        verify(notificationHistoryCacheMetrics).incrementRedisMiss();
        verify(notificationHistoryCacheMetrics).incrementDbFallback();
        verify(notificationHistoryCacheMetrics).incrementRedisError();
        verify(notificationHistoryLocalCacheRepository).put(any(NotificationHistoryCacheKey.class), eq(dbResult));
    }

    private NotificationCursorResult<NotificationHistoryQueryResult> emptyResult() {
        return new NotificationCursorResult<>(List.of(), false, null, null);
    }
}
