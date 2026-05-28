package com.mh.notification.application.service;

import com.mh.notification.application.dto.NotificationCursorResult;
import com.mh.notification.application.dto.NotificationHistoryCacheKey;
import com.mh.notification.application.dto.NotificationHistoryCacheLookup;
import com.mh.notification.application.dto.NotificationHistoryQueryResult;
import com.mh.notification.application.port.NotificationHistoryCacheMetrics;
import com.mh.notification.application.port.NotificationHistoryCacheRepository;
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
    private NotificationHistoryCacheRepository notificationHistoryCacheRepository;

    @Mock
    private NotificationHistoryCacheMetrics notificationHistoryCacheMetrics;

    @Mock
    private NotificationCursorQueryService notificationCursorQueryService;

    private CachedNotificationCursorQueryService cachedNotificationCursorQueryService;

    @BeforeEach
    void setUp() {
        cachedNotificationCursorQueryService = new CachedNotificationCursorQueryService(
                notificationHistoryCacheRepository,
                notificationHistoryCacheMetrics,
                notificationCursorQueryService
        );
    }

    @Test
    void getRecentNotifications_whenCacheHit_thenReturnCacheAndDoNotCallDelegate() {
        NotificationCursorResult<NotificationHistoryQueryResult> cachedResult = emptyResult();
        given(notificationHistoryCacheRepository.get(any(NotificationHistoryCacheKey.class)))
                .willReturn(NotificationHistoryCacheLookup.hit(cachedResult));

        NotificationCursorResult<NotificationHistoryQueryResult> result =
                cachedNotificationCursorQueryService.getRecentNotifications(1L, null, null, 20);

        assertThat(result).isSameAs(cachedResult);
        verify(notificationHistoryCacheMetrics).incrementRedisHit();
        verify(notificationCursorQueryService, never()).getRecentNotifications(anyLong(), any(), any(), anyInt());
        verify(notificationHistoryCacheRepository, never()).put(any(), any());
    }

    @Test
    void getRecentNotifications_whenCacheMiss_thenCallDelegateAndStoreCache() {
        NotificationCursorResult<NotificationHistoryQueryResult> dbResult = emptyResult();
        LocalDateTime cursorCreatedAt = LocalDateTime.of(2026, 5, 28, 10, 0);
        given(notificationHistoryCacheRepository.get(any(NotificationHistoryCacheKey.class)))
                .willReturn(NotificationHistoryCacheLookup.miss());
        given(notificationCursorQueryService.getRecentNotifications(1L, cursorCreatedAt, 10L, 20))
                .willReturn(dbResult);
        given(notificationHistoryCacheRepository.put(any(NotificationHistoryCacheKey.class), eq(dbResult)))
                .willReturn(true);

        NotificationCursorResult<NotificationHistoryQueryResult> result =
                cachedNotificationCursorQueryService.getRecentNotifications(1L, cursorCreatedAt, 10L, 20);

        assertThat(result).isSameAs(dbResult);
        verify(notificationHistoryCacheMetrics).incrementRedisMiss();
        verify(notificationHistoryCacheMetrics).incrementDbFallback();
        verify(notificationCursorQueryService).getRecentNotifications(1L, cursorCreatedAt, 10L, 20);

        ArgumentCaptor<NotificationHistoryCacheKey> keyCaptor =
                ArgumentCaptor.forClass(NotificationHistoryCacheKey.class);
        verify(notificationHistoryCacheRepository).put(keyCaptor.capture(), eq(dbResult));
        assertThat(keyCaptor.getValue()).isEqualTo(new NotificationHistoryCacheKey(1L, cursorCreatedAt, 10L, 20));
    }

    @Test
    void getRecentNotifications_whenRedisGetFails_thenFallbackToDb() {
        NotificationCursorResult<NotificationHistoryQueryResult> dbResult = emptyResult();
        given(notificationHistoryCacheRepository.get(any(NotificationHistoryCacheKey.class)))
                .willReturn(NotificationHistoryCacheLookup.error());
        given(notificationCursorQueryService.getRecentNotifications(1L, null, null, 20))
                .willReturn(dbResult);
        given(notificationHistoryCacheRepository.put(any(NotificationHistoryCacheKey.class), eq(dbResult)))
                .willReturn(true);

        NotificationCursorResult<NotificationHistoryQueryResult> result =
                cachedNotificationCursorQueryService.getRecentNotifications(1L, null, null, 20);

        assertThat(result).isSameAs(dbResult);
        verify(notificationHistoryCacheMetrics).incrementRedisError();
        verify(notificationHistoryCacheMetrics).incrementDbFallback();
        verify(notificationCursorQueryService).getRecentNotifications(1L, null, null, 20);
    }

    @Test
    void getRecentNotifications_whenRedisPutFails_thenReturnDbResult() {
        NotificationCursorResult<NotificationHistoryQueryResult> dbResult = emptyResult();
        given(notificationHistoryCacheRepository.get(any(NotificationHistoryCacheKey.class)))
                .willReturn(NotificationHistoryCacheLookup.miss());
        given(notificationCursorQueryService.getRecentNotifications(1L, null, null, 20))
                .willReturn(dbResult);
        given(notificationHistoryCacheRepository.put(any(NotificationHistoryCacheKey.class), eq(dbResult)))
                .willReturn(false);

        NotificationCursorResult<NotificationHistoryQueryResult> result =
                cachedNotificationCursorQueryService.getRecentNotifications(1L, null, null, 20);

        assertThat(result).isSameAs(dbResult);
        verify(notificationHistoryCacheMetrics).incrementRedisMiss();
        verify(notificationHistoryCacheMetrics).incrementDbFallback();
        verify(notificationHistoryCacheMetrics).incrementRedisError();
    }

    private NotificationCursorResult<NotificationHistoryQueryResult> emptyResult() {
        return new NotificationCursorResult<>(List.of(), false, null, null);
    }
}
