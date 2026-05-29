package com.mh.notification.application.service;

import com.mh.notification.application.outbox.NotificationMessagePublisher;
import com.mh.notification.application.port.NotificationOutboxRepository;
import com.mh.notification.domain.NotificationOutbox;
import com.mh.notification.domain.OutboxStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class NotificationOutboxRecoveryService {

    private final NotificationOutboxRepository notificationOutboxRepository;
    private final NotificationMessagePublisher notificationMessagePublisher;
    private final long republishGracePeriodSeconds;

    public NotificationOutboxRecoveryService(NotificationOutboxRepository notificationOutboxRepository,
                                             NotificationMessagePublisher notificationMessagePublisher,
                                             @Value("${app.outbox.republish-grace-period-seconds:60}") long republishGracePeriodSeconds) {
        this.notificationOutboxRepository = notificationOutboxRepository;
        this.notificationMessagePublisher = notificationMessagePublisher;
        this.republishGracePeriodSeconds = republishGracePeriodSeconds;
    }

    public void republishPublishedOutboxesWithoutSendResult() {
        LocalDateTime cutoff = LocalDateTime.now().minusSeconds(republishGracePeriodSeconds);
        List<NotificationOutbox> publishedOutboxes =
                notificationOutboxRepository.findPublishedWithoutSendResultCreatedBefore(cutoff);

        for (NotificationOutbox outbox : publishedOutboxes) {
            try {
                republish(outbox);
            } catch (Exception e) {
                log.error("Outbox republish failed. outboxId={}", outbox.getId(), e);
            }
        }
    }

    private void republish(NotificationOutbox outbox) {
        if (outbox.getStatus() != OutboxStatus.PUBLISHED) {
            return;
        }

        notificationMessagePublisher.publish(outbox);
    }
}
