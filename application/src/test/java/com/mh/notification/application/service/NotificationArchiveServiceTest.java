package com.mh.notification.application.service;

import com.mh.notification.application.port.ArchiveNotificationRepository;
import com.mh.notification.application.port.NotificationRepository;
import com.mh.notification.domain.Notification;
import com.mh.notification.domain.NotificationChannel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationArchiveServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private ArchiveNotificationRepository archiveNotificationRepository;

    @InjectMocks
    private NotificationArchiveService notificationArchiveService;

    @Test
    void archiveOldNotifications_whenNoOldNotifications_thenDoNothing() {
        // given
        given(notificationRepository.findOldNotifications(any(LocalDateTime.class), eq(1000)))
                .willReturn(List.of());

        // when
        notificationArchiveService.archiveOldNotifications();

        // then
        verify(archiveNotificationRepository, never()).saveAll(any());
        verify(notificationRepository, never()).deleteAllByIds(any());
    }

    @Test
    void archiveOldNotifications_whenOldNotificationsExist_thenArchiveAndDelete() {
        // given
        Notification notification = Notification.create(
                "req-1001",
                1001L,
                2001L,
                "PAYMENT",
                NotificationChannel.EMAIL,
                "결제 완료",
                "결제가 정상적으로 완료되었습니다.",
                "/payments/123"
        );

        setField(notification, "id", 1L);
        setField(notification, "createdAt", LocalDateTime.now().minusDays(8));

        given(notificationRepository.findOldNotifications(any(LocalDateTime.class), eq(1000)))
                .willReturn(List.of(notification));

        // when
        notificationArchiveService.archiveOldNotifications();

        // then
        verify(archiveNotificationRepository, times(1)).saveAll(any());
        verify(notificationRepository, times(1)).deleteAllByIds(List.of(1L));

    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
