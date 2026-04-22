package com.mh.notification.application.service;

import com.mh.notification.application.dto.ArchiveNotificationRow;
import com.mh.notification.application.port.ArchiveNotificationRepository;
import com.mh.notification.application.port.NotificationRepository;
import com.mh.notification.domain.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationArchiveService {

    private static final int ARCHIVE_BATCH_SIZE = 1000;

    private final NotificationRepository notificationRepository;
    private final ArchiveNotificationRepository archiveNotificationRepository;

    public void archiveOldNotifications() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(7);

        List<Notification> oldNotifications =
                notificationRepository.findOldNotifications(cutoff, ARCHIVE_BATCH_SIZE);

        if (oldNotifications.isEmpty()) {
            log.info("No notifications to archive.");
            return;
        }

        List<ArchiveNotificationRow> rows = oldNotifications.stream()
                .map(ArchiveNotificationRow::from)
                .toList();

        List<Long> ids = oldNotifications.stream()
                .map(Notification::getId)
                .toList();

        saveToArchive(rows);
        deleteFromMain(ids);
    }

    private void saveToArchive(List<ArchiveNotificationRow> rows) {
        archiveNotificationRepository.saveAll(rows);
        log.info("Archived {} notifications to archive DB.", rows.size());
    }

    private void deleteFromMain(List<Long> ids) {
        try {
            notificationRepository.deleteAllByIds(ids);
            log.info("Deleted {} notifications from main DB.", ids.size());
        } catch (Exception e) {
            log.error("Failed to delete notifications from main DB. ids={}", ids, e);
        }
    }

}
