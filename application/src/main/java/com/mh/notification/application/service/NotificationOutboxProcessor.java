package com.mh.notification.application.service;

import com.mh.notification.application.outbox.NotificationMessagePublisher;
import com.mh.notification.application.port.NotificationOutboxRepository;
import com.mh.notification.domain.NotificationOutbox;
import com.mh.notification.domain.OutboxStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationOutboxProcessor {

    private final NotificationOutboxRepository notificationOutboxRepository;
    private final NotificationMessagePublisher notificationMessagePublisher;

    @Transactional
    public void publishSingleOutbox(Long outboxId) {
        NotificationOutbox outbox = notificationOutboxRepository.findById(outboxId)
                .orElseThrow(() -> new IllegalArgumentException("Outbox not found. id=" + outboxId));

        if (outbox.getStatus() != OutboxStatus.PENDING) {
            return;
        }

        notificationMessagePublisher.publish(outbox);
        outbox.markPublished();
    }
}
