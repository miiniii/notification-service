package com.mh.notification.app;

import com.mh.notification.domain.FailureType;
import com.mh.notification.domain.Notification;
import com.mh.notification.domain.NotificationChannel;
import com.mh.notification.domain.NotificationSendResult;
import com.mh.notification.infrastructure.persistence.NotificationJpaRepository;
import com.mh.notification.infrastructure.persistence.NotificationSendResultJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@Profile("dummy")
@RequiredArgsConstructor
public class NotificationDummyDataLoader implements CommandLineRunner {

    private final NotificationJpaRepository notificationJpaRepository;
    private final NotificationSendResultJpaRepository notificationSendResultJpaRepository;

    private static final int TOTAL_COUNT = 100_000;
    private static final int BATCH_SIZE = 1_000;

    @Override
    public void run(String... args) {
        if (notificationJpaRepository.count() > 0) {
            return;
        }

        Random random = new Random();

        List<Notification> notificationBatch = new ArrayList<>();
        List<NotificationSendResult> sendResultBatch = new ArrayList<>();

        for (int i = 1; i <= TOTAL_COUNT; i++) {
            long requesterId = random.nextInt(1000) + 1L;
            long userId = random.nextInt(10000) + 1L;

            NotificationChannel channel = randomChannel(random);
            LocalDateTime createdAt = LocalDateTime.now()
                    .minusDays(random.nextInt(30))
                    .minusMinutes(random.nextInt(1440));

            Notification notification = Notification.create(
                    "req-" + i,
                    requesterId,
                    userId,
                    randomService(random),
                    channel,
                    "title-" + i,
                    "body-" + i,
                    "https://example.com/" + i
            );

            setCreatedAt(notification, createdAt);
            notificationBatch.add(notification);

            if (notificationBatch.size() == BATCH_SIZE) {
                saveBatch(notificationBatch, sendResultBatch, random);
                notificationBatch.clear();
                sendResultBatch.clear();
                System.out.println("inserted: " + i);
            }
        }

        if (!notificationBatch.isEmpty()) {
            saveBatch(notificationBatch, sendResultBatch, random);
        }

        System.out.println("dummy data inserted.");
    }

    @Transactional
    protected void saveBatch(List<Notification> notificationBatch,
                             List<NotificationSendResult> sendResultBatch,
                             Random random) {

        List<Notification> savedNotifications = notificationJpaRepository.saveAll(notificationBatch);

        for (Notification saved : savedNotifications) {
            boolean success = random.nextInt(100) < 85;
            LocalDateTime processedAt = saved.getCreatedAt().plusMinutes(random.nextInt(10) + 1);

            NotificationSendResult result = success
                    ? NotificationSendResult.success(saved.getId(), saved.getChannel(), 0)
                    : NotificationSendResult.failed(
                    saved.getId(),
                    saved.getChannel(),
                    random.nextInt(3) + 1,
                    randomFailureType(random),
                    500,
                    "mock failure"
            );

            setProcessedAtAndCreatedAt(result, processedAt, saved.getCreatedAt());
            sendResultBatch.add(result);
        }

        notificationSendResultJpaRepository.saveAll(sendResultBatch);
    }

    private NotificationChannel randomChannel(Random random) {
        NotificationChannel[] values = NotificationChannel.values();
        return values[random.nextInt(values.length)];
    }

    private String randomService(Random random) {
        String[] services = {"ORDER", "PAYMENT", "COUPON", "DELIVERY"};
        return services[random.nextInt(services.length)];
    }

    private FailureType randomFailureType(Random random) {
        FailureType[] values = FailureType.values();
        return values[random.nextInt(values.length)];
    }

    private void setCreatedAt(Notification notification, LocalDateTime createdAt) {
        try {
            var field = Notification.class.getDeclaredField("createdAt");
            field.setAccessible(true);
            field.set(notification, createdAt);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setProcessedAtAndCreatedAt(NotificationSendResult result,
                                            LocalDateTime processedAt,
                                            LocalDateTime createdAt) {
        try {
            var processedAtField = NotificationSendResult.class.getDeclaredField("processedAt");
            processedAtField.setAccessible(true);
            processedAtField.set(result, processedAt);

            var createdAtField = NotificationSendResult.class.getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            createdAtField.set(result, createdAt);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
