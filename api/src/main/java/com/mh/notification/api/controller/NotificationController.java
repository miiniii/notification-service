package com.mh.notification.api.controller;

import com.mh.notification.api.dto.CursorPageResponse;
import com.mh.notification.api.dto.NotificationCreateRequest;
import com.mh.notification.api.dto.NotificationCreateResponse;
import com.mh.notification.api.dto.NotificationHistoryResponse;
import com.mh.notification.application.dto.NotificationCreateResult;
import com.mh.notification.application.dto.NotificationCursorResult;
import com.mh.notification.application.dto.NotificationHistoryQueryResult;
import com.mh.notification.application.service.NotificationCursorQueryService;
import com.mh.notification.application.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationCursorQueryService notificationCursorQueryService;

    @PostMapping
    public NotificationCreateResponse createNotification(@Valid @RequestBody NotificationCreateRequest request) {
        NotificationCreateResult result = notificationService.createNotification(request.toCommand());
        return NotificationCreateResponse.from(result);
    }

    @GetMapping("/history")
    public CursorPageResponse<NotificationHistoryResponse> getNotificationHistoryWithCursor(
            @RequestParam(name = "requesterId") Long requesterId,
            @RequestParam(name = "cursorCreatedAt", required = false) LocalDateTime cursorCreatedAt,
            @RequestParam(name = "cursorId", required = false) Long cursorId,
            @RequestParam(name = "size", defaultValue = "20") int size
    ) {
        NotificationCursorResult<NotificationHistoryQueryResult> result =
                notificationCursorQueryService.getRecentNotifications(
                        requesterId,
                        cursorCreatedAt,
                        cursorId,
                        size
                );

        List<NotificationHistoryResponse> content = result.content().stream()
                .map(NotificationHistoryResponse::from)
                .toList();

        return new CursorPageResponse<>(
                content,
                result.hasNext(),
                result.nextCursorCreatedAt(),
                result.nextCursorId()
        );
    }

}
