package com.mh.notification.infrastructure.persistence;

import com.mh.notification.domain.NotificationSendResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationSendResultJpaRepository extends JpaRepository<NotificationSendResult, Long> {
}
