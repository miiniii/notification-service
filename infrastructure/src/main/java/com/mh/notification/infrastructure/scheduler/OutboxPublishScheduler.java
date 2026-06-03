package com.mh.notification.infrastructure.scheduler;

import com.mh.notification.application.service.NotificationOutboxPublishService;
import com.mh.notification.application.service.NotificationOutboxRecoveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OutboxPublishScheduler {

    private final NotificationOutboxPublishService notificationOutboxPublishService;
    private final NotificationOutboxRecoveryService notificationOutboxRecoveryService;

    @Scheduled(fixedDelay = 5000)
    public void publishPendingOutboxes() {
        notificationOutboxPublishService.publishPendingOutboxes();
        notificationOutboxRecoveryService.republishPublishedOutboxesWithoutSendResult();
    }
}
