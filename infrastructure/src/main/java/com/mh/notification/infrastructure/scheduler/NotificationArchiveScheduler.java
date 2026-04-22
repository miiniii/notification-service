package com.mh.notification.infrastructure.scheduler;

import com.mh.notification.application.service.NotificationArchiveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationArchiveScheduler {

    private final NotificationArchiveService notificationArchiveService;

    @Scheduled(cron = "0 0 2 * * *")
    public void archiveOldNotifications() {
        log.info("Start notification archive scheduler");
        notificationArchiveService.archiveOldNotifications();
        log.info("End notification archive scheduler");
    }
}
