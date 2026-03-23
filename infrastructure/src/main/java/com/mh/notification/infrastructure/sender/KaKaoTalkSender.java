package com.mh.notification.infrastructure.sender;

import com.mh.notification.application.dto.NotificationMessage;
import com.mh.notification.application.sender.NotificationSender;
import com.mh.notification.domain.NotificationChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KaKaoTalkSender implements NotificationSender {

    @Override
    public boolean supports(NotificationChannel channel) {
        return channel == NotificationChannel.KAKAO_TALK;
    }

    @Override
    public void send(NotificationMessage message) {
        log.info("[KAKAO SEND] notificationId = {}, title={}, body={}",
                message.notificationId(),
                message.title(),
                message.body());
    }
}
