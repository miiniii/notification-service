package com.mh.notification.infrastructure.redis;

import com.mh.notification.application.port.NotificationMessageStreamAcknowledger;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisNotificationMessageStreamAcknowledger implements NotificationMessageStreamAcknowledger {

    private static final String STREAM_KEY = NotificationStreamKeys.WORK;
    private static final String GROUP_NAME = "notification-group";

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void ack(String recordId) {
        stringRedisTemplate.opsForStream().acknowledge(STREAM_KEY, GROUP_NAME, recordId);
    }
}
