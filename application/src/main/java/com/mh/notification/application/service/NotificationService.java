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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationOutboxRepository notificationOutboxRepository;
    private final OutboxPayloadSerializer outboxPayloadSerializer;

    @Transactional
    public NotificationCreateResult createNotification(NotificationCreateCommand command) {
        String requestId = UUID.randomUUID().toString().replace("-", "").substring(0, 15);

        log.info("[CREATE] generated requestId={}", requestId);

        Notification notification = Notification.create(
                requestId,
                command.requesterId(),
                command.userId(),
                command.service(),
                command.channel(),
                command.title(),
                command.body(),
                command.targetUrl()
        );

        Notification savedNotification = notificationRepository.save(notification);
        log.info("[CREATE] saved notificationId={}, requestId={}",
                savedNotification.getId(),
                savedNotification.getRequestId());

        NotificationOutboxPayload payload = NotificationOutboxPayload.from(savedNotification, command.receiver());
        String payloadJson = outboxPayloadSerializer.serialize(payload);
        log.info("[OUTBOX PAYLOAD] notificationId={}, requestId={}, payload={}",
                savedNotification.getId(),
                savedNotification.getRequestId(),
                payloadJson);

        NotificationOutbox outbox = NotificationOutbox.create(
                                            savedNotification.getId(),
                                            savedNotification.getChannel(),
                                            payloadJson
                                        );

        notificationOutboxRepository.save(outbox);

        return NotificationCreateResult.from(savedNotification);
    }

}
