package com.mh.notification.infrastructure.sender;

import com.mh.notification.application.dto.NotificationMessage;
import com.mh.notification.domain.NotificationChannel;
import com.mh.notification.infrastructure.gateway.NotificationSendGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class SmsNotificationSender extends AbstractMockNotificationSender {

    public SmsNotificationSender(NotificationSendGateway notificationSendGateway) {
        super(notificationSendGateway);
    }

    @Override
    public boolean supports(NotificationChannel channel) {
        return channel == NotificationChannel.SMS;
    }

    @Override
    protected String resolveChannelType(NotificationMessage message) {
        return "SMS";
    }

    @Override
    protected String resolveReceiver(NotificationMessage message) {
        return "01012345678";
    }
}
