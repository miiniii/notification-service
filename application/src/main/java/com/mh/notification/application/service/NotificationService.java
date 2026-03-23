package com.mh.notification.application.service;

import com.mh.notification.application.dto.NotificationCreateCommand;
import com.mh.notification.application.dto.NotificationCreateResult;
import com.mh.notification.application.dto.NotificationOutboxPayload;
import com.mh.notification.application.outbox.OutboxPayloadSerializer;
import com.mh.notification.application.port.NotificationOutboxRepository;
import com.mh.notification.application.port.NotificationRepository;
import com.mh.notification.domain.Notification;
import com.mh.notification.domain.NotificationOutbox;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationOutboxRepository notificationOutboxRepository;
    private final OutboxPayloadSerializer outboxPayloadSerializer;

    @Transactional
    public NotificationCreateResult createNotification(NotificationCreateCommand command) {
        Notification notification = Notification.create(
                command.userId(),
                command.service(),
                command.channel(),
                command.title(),
                command.body(),
                command.targetUrl()
        );

        Notification savedNotification = notificationRepository.save(notification);

        NotificationOutboxPayload payload = NotificationOutboxPayload.from(savedNotification);
        String payloadJson = outboxPayloadSerializer.serialize(payload);

        NotificationOutbox outbox = NotificationOutbox.create(
                                            savedNotification.getId(),
                                            savedNotification.getChannel(),
                                            payloadJson
                                        );

        notificationOutboxRepository.save(outbox);

        return NotificationCreateResult.from(savedNotification);
    }

}
