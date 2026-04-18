package com.mh.notification.infrastructure.scheduler;


import com.mh.notification.application.service.NotificationPendingRetryService;
import com.mh.notification.application.service.NotificationStreamConsumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationRetryScheduler {

    private final NotificationPendingRetryService notificationPendingRetryService;
    private final NotificationStreamConsumeService notificationStreamConsumeService;

    @Scheduled(fixedDelay = 5000)
    public void retryPendingMessages() {
        notificationPendingRetryService.retryPendingMessages();
    }

    @Scheduled(fixedDelay = 5000)
    public void reclaimPendingMessages() {
        notificationStreamConsumeService.reclaimPendingMessages();
    }
}
