package com.mh.notification.application.service;

import com.mh.notification.application.dto.ConsumeResult;
import com.mh.notification.application.dto.NotificationMessage;
import com.mh.notification.application.dto.StreamMessage;
import com.mh.notification.application.port.NotificationMessageDeserializer;
import com.mh.notification.application.port.NotificationMessageStreamAcknowledger;
import com.mh.notification.application.port.NotificationPendingMessageReclaimer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationPendingRetryService {

    private final NotificationPendingMessageReclaimer notificationPendingMessageReclaimer;
    private final NotificationMessageDeserializer notificationMessageDeserializer;
    private final NotificationMessageConsumer notificationMessageConsumer;
    private final NotificationMessageStreamAcknowledger notificationMessageStreamAcknowledger;

    public void retryPendingMessages() {
        List<StreamMessage> pendingMessages = notificationPendingMessageReclaimer.reclaimPendingMessages();

        if (pendingMessages.isEmpty()) {
            return;
        }

        log.info("[PEL RETRY] pending message count={}", pendingMessages.size());

        for (StreamMessage streamMessage : pendingMessages) {
            NotificationMessage message =
                    notificationMessageDeserializer.deserialize(streamMessage.payload());

            ConsumeResult result = notificationMessageConsumer.consume(message);

            if (result == ConsumeResult.SUCCESS
                    || result == ConsumeResult.ALREADY_PROCESSED
                    || result == ConsumeResult.RETRYABLE_FAIL
                    || result == ConsumeResult.DEAD_LETTERED) {
                notificationMessageStreamAcknowledger.ack(streamMessage.recordId());

                log.info("[PEL RETRY ACK] notificationId={}, channel={}, result={}",
                        message.notificationId(), message.channel(), result);
            } else {
                log.info("[PEL RETRY KEEP] notificationId={}, channel={}, result={}",
                        message.notificationId(), message.channel(), result);
            }
        }
    }
}