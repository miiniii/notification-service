package com.mh.notification.application.service;

import com.mh.notification.application.dto.NotificationCursorResult;
import com.mh.notification.application.dto.NotificationHistoryQueryResult;
import com.mh.notification.application.exception.InvalidCursorException;
import com.mh.notification.application.port.NotificationRepository;
import com.mh.notification.application.port.NotificationSendResultRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationCursorQueryServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationSendResultRepository notificationSendResultRepository;

    @InjectMocks
    private NotificationCursorQueryService notificationCursorQueryService;

    @Test
    void getRecentNotifications_whenOnlyCursorCreatedAtExists_thenThrowInvalidCursorException() {
        LocalDateTime cursorCreatedAt = LocalDateTime.of(2026, 5, 28, 10, 0);

        assertThatThrownBy(() -> notificationCursorQueryService.getRecentNotifications(
                1L,
                cursorCreatedAt,
                null,
                20
        )).isInstanceOf(InvalidCursorException.class)
                .hasMessage(InvalidCursorException.MESSAGE);

        verify(notificationRepository, never()).findRecentByRequesterIdWithCursor(anyLong(), any(), any(), any(), anyInt());
        verify(notificationSendResultRepository, never()).findByNotificationIds(any());
    }

    @Test
    void getRecentNotifications_whenOnlyCursorIdExists_thenThrowInvalidCursorException() {
        assertThatThrownBy(() -> notificationCursorQueryService.getRecentNotifications(
                1L,
                null,
                10L,
                20
        )).isInstanceOf(InvalidCursorException.class)
                .hasMessage(InvalidCursorException.MESSAGE);

        verify(notificationRepository, never()).findRecentByRequesterIdWithCursor(anyLong(), any(), any(), any(), anyInt());
        verify(notificationSendResultRepository, never()).findByNotificationIds(any());
    }

    @Test
    void getRecentNotifications_whenNoCursorExists_thenCallRepositoryWithNullCursor() {
        given(notificationRepository.findRecentByRequesterIdWithCursor(
                eq(1L),
                any(LocalDateTime.class),
                isNull(),
                isNull(),
                eq(21)
        )).willReturn(List.of());

        NotificationCursorResult<NotificationHistoryQueryResult> result =
                notificationCursorQueryService.getRecentNotifications(1L, null, null, 20);

        assertThat(result.content()).isEmpty();
        assertThat(result.hasNext()).isFalse();
        assertThat(result.nextCursorCreatedAt()).isNull();
        assertThat(result.nextCursorId()).isNull();
        verify(notificationSendResultRepository, never()).findByNotificationIds(any());
    }

    @Test
    void getRecentNotifications_whenBothCursorValuesExist_thenCallRepositoryWithCursor() {
        LocalDateTime cursorCreatedAt = LocalDateTime.of(2026, 5, 28, 10, 0);

        given(notificationRepository.findRecentByRequesterIdWithCursor(
                eq(1L),
                any(LocalDateTime.class),
                eq(cursorCreatedAt),
                eq(10L),
                eq(21)
        )).willReturn(List.of());

        NotificationCursorResult<NotificationHistoryQueryResult> result =
                notificationCursorQueryService.getRecentNotifications(1L, cursorCreatedAt, 10L, 20);

        assertThat(result.content()).isEmpty();
        assertThat(result.hasNext()).isFalse();
        assertThat(result.nextCursorCreatedAt()).isNull();
        assertThat(result.nextCursorId()).isNull();
        verify(notificationSendResultRepository, never()).findByNotificationIds(any());
    }
}
