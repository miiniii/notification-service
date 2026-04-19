package com.mh.notification.infrastructure.persistence;

import com.mh.notification.domain.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationJpaRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByRequesterIdAndCreatedAtGreaterThanEqual(
            Long requesterId,
            LocalDateTime createdAt,
            Pageable pageable
    );

    List<Notification> findByIdIn(List<Long> ids);

    @Query("""
    SELECT n
    FROM Notification n
    WHERE n.requesterId = :requesterId
      AND n.createdAt >= :from
      AND (
            :cursorCreatedAt IS NULL
            OR n.createdAt < :cursorCreatedAt
            OR (n.createdAt = :cursorCreatedAt AND n.id < :cursorId)
          )
    ORDER BY n.createdAt DESC, n.id DESC
    """)
    List<Notification> findRecentByRequesterIdWithCursor(
            @Param("requesterId") Long requesterId,
            @Param("from") LocalDateTime from,
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

}
