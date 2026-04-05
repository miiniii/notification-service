package com.mh.notification.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "notification_send_result")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationSendResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "notification_id", nullable = false)
    private Long notificationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationChannel channel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SendStatus status;

    @Column(name = "failure_reason")
    private String failureReason;

    @Enumerated(EnumType.STRING)
    private FailureType failureType;

    private Integer failureStatusCode;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "processed_at", nullable = false)
    private LocalDateTime processedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    private NotificationSendResult(Long notificationId,
                                   NotificationChannel channel,
                                   SendStatus status,
                                   int retryCount,
                                   String failureReason,
                                   FailureType failureType,
                                   Integer failureStatusCode,
                                   LocalDateTime processedAt) {
        this.notificationId = notificationId;
        this.channel = channel;
        this.status = status;
        this.retryCount = retryCount;
        this.failureReason = failureReason;
        this.failureType = failureType;
        this.failureStatusCode = failureStatusCode;
        this.processedAt = processedAt;
    }

    public static NotificationSendResult success(Long notificationId, NotificationChannel channel, int retryCount) {
        LocalDateTime now = LocalDateTime.now();

        return NotificationSendResult.builder()
                .notificationId(notificationId)
                .channel(channel)
                .status(SendStatus.SUCCESS)
                .retryCount(retryCount)
                .failureReason(null)
                .failureType(null)
                .failureStatusCode(null)
                .processedAt(now)
                .build();
    }

    public static NotificationSendResult failed(Long notificationId, NotificationChannel channel, int retryCount, FailureType failureType, Integer failureStatusCode, String failureReason) {
        LocalDateTime now = LocalDateTime.now();

        return NotificationSendResult.builder()
                .notificationId(notificationId)
                .channel(channel)
                .status(SendStatus.FAILED)
                .retryCount(retryCount)
                .failureReason(failureReason)
                .failureType(failureType)
                .failureStatusCode(failureStatusCode)
                .processedAt(now)
                .build();
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

}
