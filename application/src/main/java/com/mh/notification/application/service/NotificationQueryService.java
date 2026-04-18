package com.mh.notification.application.service;

import com.mh.notification.application.dto.NotificationHistoryQueryResult;
import com.mh.notification.application.dto.NotificationSendResultQueryResult;
import com.mh.notification.application.port.NotificationRepository;
import com.mh.notification.application.port.NotificationSendResultRepository;
import com.mh.notification.domain.Notification;
import com.mh.notification.domain.NotificationSendResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationQueryService {

    private final NotificationRepository notificationRepository;
    private final NotificationSendResultRepository notificationSendResultRepository;

    public Page<NotificationHistoryQueryResult> getRecentNotifications(Long requesterId, Pageable pageable) {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

        Page<Notification> notificationPage =
                notificationRepository.findRecentByRequesterId(requesterId, sevenDaysAgo, pageable);

        List<Long> notificationIds = notificationPage.getContent().stream()
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
        List<NotificationHistoryQueryResult> content = notificationPage.getContent().stream()
                .map(notification -> NotificationHistoryQueryResult.of(
                        notification,
                        sendResultMap.getOrDefault(notification.getId(), List.of())
                ))
                .toList();

        return new PageImpl<>(content, pageable, notificationPage.getTotalElements());
    }
}
