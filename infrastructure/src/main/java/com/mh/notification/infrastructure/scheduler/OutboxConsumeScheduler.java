package com.mh.notification.infrastructure.scheduler;

import com.mh.notification.application.service.NotificationStreamConsumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OutboxConsumeScheduler {

    private final NotificationStreamConsumeService notificationStreamConsumeService;

    @Scheduled(fixedDelay = 3000)
    public void consumeMessages() {
        notificationStreamConsumeService.consumeOnce();
    }
}
