package com.mh.notification.infrastructure.persistence;

import com.mh.notification.domain.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationJpaRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByRequesterIdAndCreatedAtGreaterThanEqual(
            Long requesterId,
            LocalDateTime createdAt,
            Pageable pageable
    );

    List<Notification> findByIdIn(List<Long> ids);

}
