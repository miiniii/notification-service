package com.mh.notification.domain;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "notification_outbox")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationOutbox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "notification_id", nullable = false)
    private Long notificationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationChannel channel;

    @Lob
    @Column(nullable = false)
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OutboxStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    private NotificationOutbox(Long notificationId,
                               NotificationChannel channel,
                               String payload,
                               OutboxStatus status) {
        this.notificationId = notificationId;
        this.channel = channel;
        this.payload = payload;
        this.status = status;
    }

    public static NotificationOutbox create(Long notificationId,
                                            NotificationChannel channel,
                                            String payload) {
        return NotificationOutbox.builder()
                .notificationId(notificationId)
                .channel(channel)
                .payload(payload)
                .status(OutboxStatus.PENDING)
                .build();
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public void markPublished() {
        this.status = OutboxStatus.PUBLISHED;
    }

    public void markFailed() {
        this.status = OutboxStatus.FAILED;
    }
}
