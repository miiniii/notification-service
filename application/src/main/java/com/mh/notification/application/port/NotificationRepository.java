package com.mh.notification.application.port;

import com.mh.notification.domain.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepository {

    Notification save(Notification notification);

    Page<Notification> findRecentByRequesterId(Long requesterId, LocalDateTime from, Pageable pageable);

    List<Notification> findByIds(List<Long> ids);
}
