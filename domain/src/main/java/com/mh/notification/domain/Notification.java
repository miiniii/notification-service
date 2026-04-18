package com.mh.notification.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "notifications")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_id", nullable = false, length = 50)
    private String requestId;

    @Column(name = "requester_id", nullable = false)
    private Long requesterId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 50)
    private String service;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationChannel channel;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 1000)
    private String body;

    @Column(name = "target_url", nullable = false, length = 500)
    private String targetUrl;

    @Column(name = "is_read", nullable = false)
    private boolean isRead;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    private Notification(String requestId,
                         Long requesterId,
                         Long userId,
                         String service,
                         NotificationChannel channel,
                         String title,
                         String body,
                         String targetUrl,
                         boolean isRead) {
        this.requestId = requestId;
        this.requesterId = requesterId;
        this.userId = userId;
        this.service = service;
        this.channel = channel;
        this.title = title;
        this.body = body;
        this.targetUrl = targetUrl;
        this.isRead = isRead;
    }

    public static Notification create(String requestId,
                                      Long requesterId,
                                      Long userId,
                                      String service,
                                      NotificationChannel channel,
                                      String title,
                                      String body,
                                      String targetUrl) {
        return Notification.builder()
                .requestId(requestId)
                .requesterId(requesterId)
                .userId(userId)
                .service(service)
                .channel(channel)
                .title(title)
                .body(body)
                .targetUrl(targetUrl)
                .isRead(false)
                .build();
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public void markAsRead() {
        this.isRead = true;
    }

}
