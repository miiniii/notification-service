package com.mh.notification.application.service;


import com.mh.notification.application.port.NotificationOutboxRepository;
import com.mh.notification.domain.NotificationOutbox;
import com.mh.notification.domain.OutboxStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationOutboxPublishService {

    private final NotificationOutboxRepository notificationOutboxRepository;
    private final NotificationOutboxProcessor notificationOutboxProcessor;

    public void publishPendingOutboxes() {
        List<NotificationOutbox> pendingOutboxes = notificationOutboxRepository.findAllByStatus(OutboxStatus.PENDING);

        for (NotificationOutbox outbox : pendingOutboxes) {
            try {
                notificationOutboxProcessor.publishSingleOutbox(outbox.getId());
            } catch (Exception e) {
                log.error("Outbox publish failed. outboxId={}", outbox.getId(), e);
            }
        }
    }

}
