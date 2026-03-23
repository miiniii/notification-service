package com.mh.notification.application.port;

import com.mh.notification.domain.NotificationSendResult;

public interface NotificationSendResultRepository {
    NotificationSendResult save(NotificationSendResult notificationSendResult);
}
