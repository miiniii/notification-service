package com.mh.notification.infrastructure.persistence;

import com.mh.notification.domain.NotificationOutbox;
import com.mh.notification.domain.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationOutboxJpaRepository extends JpaRepository<NotificationOutbox, Long> {

    List<NotificationOutbox> findAllByStatus(OutboxStatus status);

    @Query("""
            select outbox
            from NotificationOutbox outbox
            where outbox.status = com.mh.notification.domain.OutboxStatus.PUBLISHED
              and outbox.createdAt < :cutoff
              and not exists (
                  select 1
                  from NotificationSendResult result
                  where result.notificationId = outbox.notificationId
              )
            order by outbox.createdAt asc
            """)
    List<NotificationOutbox> findPublishedWithoutSendResultCreatedBefore(@Param("cutoff") LocalDateTime cutoff);
}
