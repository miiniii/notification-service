package com.mh.notification.infrastructure.persistence;

import com.mh.notification.domain.Notification;
import com.mh.notification.domain.NotificationOutbox;
import com.mh.notification.domain.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationOutboxJpaRepository extends JpaRepository<NotificationOutbox, Long> {

    List<NotificationOutbox> findAllByStatus(OutboxStatus status);

    List<Notification> findTop1000ByCreatedAtBeforeOrderByCreatedAtAsc(LocalDateTime cutoff);
}
