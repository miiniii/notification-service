package com.mh.notification.infrastructure.scheduler;

import com.mh.notification.application.service.NotificationArchiveService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class NotificationArchiveSchedulerTest {

    @Mock
    private NotificationArchiveService notificationArchiveService;

    @InjectMocks
    private NotificationArchiveScheduler notificationArchiveScheduler;

    @Test
    void archiveOldNotifications_shouldCallArchiveService() {
        // when
        notificationArchiveScheduler.archiveOldNotifications();

        // then
        verify(notificationArchiveService, times(1)).archiveOldNotifications();
    }
}
