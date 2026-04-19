package com.mh.notification.application.service;

import com.mh.notification.application.dto.NotificationCursorResult;
import com.mh.notification.application.dto.NotificationHistoryQueryResult;
import com.mh.notification.application.dto.NotificationSendResultQueryResult;
import com.mh.notification.application.port.NotificationRepository;
import com.mh.notification.application.port.NotificationSendResultRepository;
import com.mh.notification.domain.Notification;
import com.mh.notification.domain.NotificationSendResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationCursorQueryService {

    private final NotificationRepository notificationRepository;
    private final NotificationSendResultRepository notificationSendResultRepository;

    public NotificationCursorResult<NotificationHistoryQueryResult> getRecentNotifications(
            Long requesterId,
            LocalDateTime cursorCreatedAt,
            Long cursorId,
            int size
    ) {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

        List<Notification> notifications = notificationRepository.findRecentByRequesterIdWithCursor(
                requesterId,
                sevenDaysAgo,
                cursorCreatedAt,
                cursorId,
                size + 1
        );

        boolean hasNext = notifications.size() > size;

        if (hasNext) {
            notifications = notifications.subList(0, size);
        }

        List<Long> notificationIds = notifications.stream()
                .map(Notification::getId)
                .toList();

        List<NotificationSendResult> sendResults = notificationIds.isEmpty()
                ? List.of()
                : notificationSendResultRepository.findByNotificationIds(notificationIds);

        Map<Long, List<NotificationSendResultQueryResult>> sendResultMap =
                sendResults.stream()
                        .collect(Collectors.groupingBy(
                                NotificationSendResult::getNotificationId,
                                Collectors.mapping(NotificationSendResultQueryResult::from, Collectors.toList())
                        ));

        List<NotificationHistoryQueryResult> content = notifications.stream()
                .map(notification -> NotificationHistoryQueryResult.of(
                        notification,
                        sendResultMap.getOrDefault(notification.getId(), List.of())
                ))
                .toList();

        LocalDateTime nextCursorCreatedAt = null;
        Long nextCursorId = null;

        if (!content.isEmpty()) {
            NotificationHistoryQueryResult last = content.get(content.size() - 1);
            nextCursorCreatedAt = last.createdAt();
            nextCursorId = last.notificationId();
        }

        return new NotificationCursorResult<>(
                content,
                hasNext,
                nextCursorCreatedAt,
                nextCursorId
        );
    }
}
