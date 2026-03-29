package com.mh.notification.infrastructure.redis;

import com.mh.notification.application.outbox.NotificationMessagePublisher;
import com.mh.notification.domain.NotificationOutbox;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class RedisStreamNotificationMessagePublisher implements NotificationMessagePublisher {

    private static final String STREAM_KEY = "notification:work";

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void publish(NotificationOutbox outbox) {
        MapRecord<String, String, String> record = MapRecord.create(
                STREAM_KEY,
                Map.of(
                        "outboxId", String.valueOf(outbox.getId()),
                        "notificationId", String.valueOf(outbox.getNotificationId()),
                        "channel", outbox.getChannel().name(),
                        "payload", outbox.getPayload()
                )
        );

        stringRedisTemplate.opsForStream().add(record);
    }
}
