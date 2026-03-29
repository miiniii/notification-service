package com.mh.notification.infrastructure.redis;

import com.mh.notification.application.port.WaitMessageStreamDeleter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisWaitMessageStreamDeleter implements WaitMessageStreamDeleter {

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void delete(String recordId) {
        stringRedisTemplate.opsForStream().delete(NotificationStreamKeys.WAIT, recordId);
    }
}
