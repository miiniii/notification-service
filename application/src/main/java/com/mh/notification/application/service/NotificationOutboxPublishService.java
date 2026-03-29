package com.mh.notification.application.service;


import com.mh.notification.application.outbox.NotificationMessagePublisher;
import com.mh.notification.application.port.NotificationOutboxRepository;
import com.mh.notification.domain.NotificationOutbox;
import com.mh.notification.domain.OutboxStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationOutboxPublishService {

    private final NotificationOutboxRepository notificationOutboxRepository;
    private final NotificationMessagePublisher notificationMessagePublisher;

    @Transactional
    public void publishPendingOutboxes() {
        List<NotificationOutbox> pendingOutboxes = notificationOutboxRepository.findAllByStatus(OutboxStatus.PENDING);

        for (NotificationOutbox outbox : pendingOutboxes) {
            notificationMessagePublisher.publish(outbox);
            outbox.markPublished();
        }
    }
}
