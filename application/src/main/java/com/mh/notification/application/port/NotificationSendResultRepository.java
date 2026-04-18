package com.mh.notification.application.port;

import com.mh.notification.domain.NotificationChannel;
import com.mh.notification.domain.NotificationSendResult;

import java.util.List;

public interface NotificationSendResultRepository {

    NotificationSendResult save(NotificationSendResult notificationSendResult);

    boolean existsSuccessByNotificationIdAndChannel(Long notificationId, NotificationChannel channel);

    List<NotificationSendResult> findByNotificationIds(List<Long> notificationIds);
}
