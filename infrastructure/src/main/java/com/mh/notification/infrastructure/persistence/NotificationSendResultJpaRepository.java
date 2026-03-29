package com.mh.notification.infrastructure.persistence;

import com.mh.notification.domain.NotificationChannel;
import com.mh.notification.domain.NotificationSendResult;
import com.mh.notification.domain.SendStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationSendResultJpaRepository extends JpaRepository<NotificationSendResult, Long> {

    boolean existsByNotificationIdAndChannelAndStatus(Long notificationId, NotificationChannel channel, SendStatus status);
}
