package com.mh.notification.infrastructure.sender;

import com.mh.notification.application.dto.NotificationMessage;
import com.mh.notification.application.sender.NotificationSender;
import com.mh.notification.domain.NotificationChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EmailNotificationSender implements NotificationSender {

    @Override
    public boolean supports(NotificationChannel channel) {
        return channel == NotificationChannel.EMAIL;
    }

    @Override
    public void send(NotificationMessage message) {
        // 실패 테스트용
        if (message.title() != null && message.title().contains("FAIL")) {
            throw new IllegalStateException("Email provider error");
        }
        log.info("[EMAIL SEND] notificationId = {}, title={}, body={}",
                message.notificationId(),
                message.title(),
                message.body());
    }
}
