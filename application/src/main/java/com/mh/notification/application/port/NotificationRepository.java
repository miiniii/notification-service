package com.mh.notification.application.port;

import com.mh.notification.domain.Notification;

public interface NotificationRepository {

    Notification save(Notification notification);
}
