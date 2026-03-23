package com.mh.notification.application.sender;

import com.mh.notification.application.dto.NotificationMessage;
import com.mh.notification.domain.NotificationChannel;

public interface NotificationSender {

    boolean supports(NotificationChannel channel);
    void send(NotificationMessage message);
}
