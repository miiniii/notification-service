package com.mh.notification.api.controller;

import com.mh.notification.application.exception.InvalidCursorException;
import com.mh.notification.application.service.NotificationCursorQueryService;
import com.mh.notification.application.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationCursorQueryService notificationCursorQueryService;

    @BeforeEach
    void setUp() {
        NotificationController notificationController =
                new NotificationController(notificationService, notificationCursorQueryService);

        mockMvc = MockMvcBuilders.standaloneSetup(notificationController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getNotificationHistory_whenOnlyCursorCreatedAtExists_thenReturnBadRequest() throws Exception {
        given(notificationCursorQueryService.getRecentNotifications(
                eq(1L),
                any(),
                isNull(),
                eq(20)
        )).willThrow(new InvalidCursorException());

        mockMvc.perform(get("/api/notifications/history")
                        .param("requesterId", "1")
                        .param("cursorCreatedAt", "2026-05-28T10:00:00"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", containsString(InvalidCursorException.MESSAGE)));
    }

    @Test
    void getNotificationHistory_whenOnlyCursorIdExists_thenReturnBadRequest() throws Exception {
        given(notificationCursorQueryService.getRecentNotifications(
                eq(1L),
                isNull(),
                eq(10L),
                eq(20)
        )).willThrow(new InvalidCursorException());

        mockMvc.perform(get("/api/notifications/history")
                        .param("requesterId", "1")
                        .param("cursorId", "10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", containsString(InvalidCursorException.MESSAGE)));
    }
}
