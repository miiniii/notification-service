package com.mh.notification.infrastructure.sender;

import com.mh.notification.application.dto.NotificationMessage;
import com.mh.notification.domain.NotificationChannel;
import com.mh.notification.infrastructure.client.mock.MockApiClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EmailNotificationSender extends AbstractMockNotificationSender {

    public EmailNotificationSender(MockApiClient mockApiClient) {
        super(mockApiClient);
    }

    @Override
    public boolean supports(NotificationChannel channel) {
        return channel == NotificationChannel.EMAIL;
    }

//    @Override
//    public void send(NotificationMessage message) {
//        // 실패 테스트용
//        if (message.title() != null && message.title().contains("FAIL")) {
//            throw new IllegalStateException("Email provider error");
//        }
//    }

    /**
     * receiver 더미데이터
     */
    @Override
    protected String resolveChannelType(NotificationMessage message) {
        return "EMAIL";
    }

    @Override
    protected String resolveReceiver(NotificationMessage message) {
        return "test-email@example.com";
    }
}
