package com.mh.notification.application.service;

import com.mh.notification.application.dto.NotificationMessage;
import com.mh.notification.application.outbox.NotificationQueuePublisher;
import com.mh.notification.application.port.NotificationDistributedLockManager;
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
    private static final long RETRY_DELAY_SECONDS = 30L;
    private static final long LOCK_WAIT_MILLIS = 100L;
    private static final long LOCK_LEASE_MILLIS = 5000L;

    private final NotificationSendResultRepository notificationSendResultRepository;
    private final NotificationQueuePublisher notificationQueuePublisher;
    private final NotificationDistributedLockManager notificationDistributedLockManager;
    private final List<NotificationSender> notificationSenders;

    @Transactional
    public void consume(NotificationMessage message) {
        String lockKey = buildLockKey(message);

        log.info("[LOCK TRY] notificationId={}, channel={}, lockKey={}",
                message.notificationId(), message.channel(), lockKey);

        boolean locked = notificationDistributedLockManager.tryLock(lockKey, LOCK_WAIT_MILLIS, LOCK_LEASE_MILLIS);

        if (!locked) {
            log.info("[LOCK FAIL] already processing. notificationId = {}, channel={}", message.notificationId(), message.channel());
            return;
        }

        log.info("[LOCK OK] notificationId={}, channel={}, lockKey={}",
                message.notificationId(), message.channel(), lockKey);

        log.info("[LOCK OK] notificationId={}, channel={}, lockKey={}",
                message.notificationId(), message.channel(), lockKey);

        try {
            if (isAlreadySent(message)) {
                log.info("[IDEMPOTENT] already sent. notificationID={}, channel={}", message.notificationId(), message.channel());
                return;
            }
            NotificationSender sender = findSender(message);
            NotificationSendResult result = sendSafely(sender, message);

            if (result.getStatus() == SendStatus.SUCCESS) {
                notificationSendResultRepository.save(result);
                return;
            }

            if (message.retryCount() < MAX_RETRY_COUNT) {
                NotificationMessage retryMessage = message.incrementRetry(LocalDateTime.now().plusSeconds(RETRY_DELAY_SECONDS));
                notificationQueuePublisher.publishToWait(retryMessage);

                log.info("[RETRY] moved to WAIT. notificationId={}, channel={}, retryCount={}", message.notificationId(), message.channel(), retryMessage.retryCount());
                return;
            }

            notificationQueuePublisher.publishToDead(message);
            notificationSendResultRepository.save(result);

            log.info("[DEAD] moved to DEAD. notificationId={}, channel={}", message.notificationId(), message.channel());

        } finally {
            notificationDistributedLockManager.unlock(lockKey);
        }

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
            //Thread.sleep(5000); //락 테스트용
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

    private String buildLockKey(NotificationMessage message) {
        return "lock:notification:" + message.notificationId() + ":" + message.channel().name();
    }
}
