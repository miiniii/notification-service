package com.mh.notification.infrastructure.persistence;

import com.mh.notification.application.port.NotificationSendResultRepository;
import com.mh.notification.domain.NotificationChannel;
import com.mh.notification.domain.NotificationSendResult;
import com.mh.notification.domain.SendStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class NotificationSendResultRepositoryImpl implements NotificationSendResultRepository {

    private final NotificationSendResultJpaRepository notificationSendResultJpaRepository;

    @Override
    public NotificationSendResult save(NotificationSendResult notificationSendResult) {
        return notificationSendResultJpaRepository.save(notificationSendResult);
    }

    @Override
    public boolean existsSuccessByNotificationIdAndChannel(Long notificationId, NotificationChannel channel) {
        return notificationSendResultJpaRepository.existsByNotificationIdAndChannelAndStatus(notificationId, channel, SendStatus.SUCCESS);
    }

    @Override
    public List<NotificationSendResult> findByNotificationIds(List<Long> notificationIds) {
        return notificationSendResultJpaRepository.findByNotificationIdIn(notificationIds);
    }

}
