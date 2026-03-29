package com.mh.notification.infrastructure.redis;

import com.mh.notification.application.port.NotificationMessageStreamDeleter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisNotificationMessageStreamDeleter implements NotificationMessageStreamDeleter {

    private static final String STREAM_KEY = "notification:work";

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void delete(String recordId) {
        stringRedisTemplate.opsForStream().delete(STREAM_KEY, recordId);
    }
}
