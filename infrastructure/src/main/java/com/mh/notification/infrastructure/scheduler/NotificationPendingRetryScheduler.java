package com.mh.notification.infrastructure.scheduler;


import com.mh.notification.application.service.NotificationPendingRetryService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationPendingRetryScheduler {

    private final NotificationPendingRetryService notificationPendingRetryService;

    @Scheduled(fixedDelay = 5000)
    public void retryPendingMessages() {
        notificationPendingRetryService.retryPendingMessages();
    }
}
