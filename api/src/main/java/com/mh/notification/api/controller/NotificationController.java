package com.mh.notification.api.controller;

import com.mh.notification.api.dto.NotificationCreateRequest;
import com.mh.notification.api.dto.NotificationCreateResponse;
import com.mh.notification.application.dto.NotificationCreateResult;
import com.mh.notification.application.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    public NotificationCreateResponse createNotification(@Valid @RequestBody NotificationCreateRequest request) {
        NotificationCreateResult result = notificationService.createNotification(request.toCommand());
        return NotificationCreateResponse.from(result);
    }
}
