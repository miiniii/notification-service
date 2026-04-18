package com.mh.notification.api.controller;

import com.mh.notification.api.dto.NotificationCreateRequest;
import com.mh.notification.api.dto.NotificationCreateResponse;
import com.mh.notification.api.dto.NotificationHistoryResponse;
import com.mh.notification.application.dto.NotificationCreateResult;
import com.mh.notification.application.dto.NotificationHistoryQueryResult;
import com.mh.notification.application.service.NotificationQueryService;
import com.mh.notification.application.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationQueryService notificationQueryService;

    @PostMapping
    public NotificationCreateResponse createNotification(@Valid @RequestBody NotificationCreateRequest request) {
        NotificationCreateResult result = notificationService.createNotification(request.toCommand());
        return NotificationCreateResponse.from(result);
    }

    @GetMapping("/history")
    public Page<NotificationHistoryResponse> getNotificationHistory(
            @RequestParam(name = "requesterId") Long requesterId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(
                        Sort.Order.desc("createdAt"),
                        Sort.Order.desc("id")
                )
        );

        Page<NotificationHistoryQueryResult> resultPage =
                notificationQueryService.getRecentNotifications(requesterId, pageable);

        return resultPage.map(NotificationHistoryResponse::from);
    }
}
