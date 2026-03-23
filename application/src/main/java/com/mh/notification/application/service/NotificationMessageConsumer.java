package com.mh.notification.application.service;

import com.mh.notification.application.dto.NotificationMessage;
import com.mh.notification.application.outbox.NotificationQueuePublisher;
import com.mh.notification.application.port.NotificationSendResultRepository;
import com.mh.notification.application.sender.NotificationSender;
import com.mh.notification.domain.NotificationSendResult;
import com.mh.notification.domain.SendStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationMessageConsumer {

    private static final int MAX_RETRY_COUNT = 3;

    private final NotificationSendResultRepository notificationSendResultRepository;
    private final NotificationQueuePublisher notificationQueuePublisher;
    private final List<NotificationSender> notificationSenders;

    @Transactional
    public void consume(NotificationMessage message) {

        if (isAlreadySent(message)) {
            log.info("[IDEMPOTENT] already sent. notificationID={}, channer={}", message.notificationId(), message.channel());
            return;
        }
        NotificationSender sender = findSender(message);

        NotificationSendResult result = sendSafely(sender, message);

        if (result.getStatus() == SendStatus.SUCCESS) {
            notificationSendResultRepository.save(result);
            return;
        }

        if (message.retryCount() < MAX_RETRY_COUNT) {
            NotificationMessage retryMessage = message.incrementRetry(LocalDateTime.now().plusSeconds(30));
            notificationQueuePublisher.publishToWait(retryMessage);
            return;
        }

        notificationQueuePublisher.publishToDead(message);
        notificationSendResultRepository.save(result);
    }

    private boolean isAlreadySent(NotificationMessage message) {
        return notificationSendResultRepository.existsSuccessByNotificationIdAndChannel(message.notificationId(), message.channel());
    }

    private NotificationSender findSender(NotificationMessage message) {
        return notificationSenders.stream()
                .filter(s -> s.supports(message.channel()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No sender found for channel: " + message.channel()));
    }

    private NotificationSendResult sendSafely(NotificationSender sender, NotificationMessage message) {
        try {
            sender.send(message);
            return NotificationSendResult.success(message.notificationId(), message.channel());
        } catch (Exception e) {
            String failureReason = extractFailureReason(e);
            return NotificationSendResult.failed(message.notificationId(), message.channel(), failureReason);
        }
    }

    private String extractFailureReason(Exception e) {
        if (e.getMessage() != null && !e.getMessage().isBlank()) {
            return e.getMessage();
        }
        return e.getClass().getSimpleName();
    }
}
