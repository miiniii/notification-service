package com.mh.notification.infrastructure.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mh.notification.application.dto.NotificationMessage;
import com.mh.notification.application.outbox.NotificationMessageSerializer;
import com.mh.notification.application.outbox.NotificationQueuePublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class RedisNotificationQueuePublisher implements NotificationQueuePublisher {

    private final StringRedisTemplate stringRedisTemplate;
    private final NotificationMessageSerializer notificationMessageSerializer;

    @Override
    public void publishToWork(NotificationMessage message) {
        add(NotificationStreamKeys.WORK, message);
    }

    @Override
    public void publishToWait(NotificationMessage message) {
        add(NotificationStreamKeys.WAIT, message);
    }

    @Override
    public void publishToDead(NotificationMessage message) {
        add(NotificationStreamKeys.DEAD, message);
    }

    private void add(String streamKey, NotificationMessage message) {
        String payload = notificationMessageSerializer.serialize(message);

        MapRecord<String, String, String> record = MapRecord.create(streamKey, Map.of("payload", payload));

        stringRedisTemplate.opsForStream().add(record);
    }

}
