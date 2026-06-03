package com.mh.notification.infrastructure.redis;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mh.notification.application.dto.NotificationCursorResult;
import com.mh.notification.application.dto.NotificationHistoryCacheKey;
import com.mh.notification.application.dto.NotificationHistoryCacheLookup;
import com.mh.notification.application.dto.NotificationHistoryQueryResult;
import com.mh.notification.application.port.NotificationHistoryCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Slf4j
@Repository
@RequiredArgsConstructor
public class RedisNotificationHistoryCacheRepository implements NotificationHistoryCacheRepository {

    private static final String KEY_PREFIX = "notification:history";

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final NotificationHistoryCacheProperties notificationHistoryCacheProperties;

    @Override
    public NotificationHistoryCacheLookup get(NotificationHistoryCacheKey key) {
        try {
            String value = stringRedisTemplate.opsForValue().get(toRedisKey(key));
            if (value == null) {
                return NotificationHistoryCacheLookup.miss();
            }

            return NotificationHistoryCacheLookup.hit(objectMapper.readValue(value, cacheValueType()));
        } catch (Exception e) {
            log.warn("[CACHE GET FAIL] notification history cache get failed. key={}", key, e);
            return NotificationHistoryCacheLookup.error();
        }
    }

    @Override
    public boolean put(
            NotificationHistoryCacheKey key,
            NotificationCursorResult<NotificationHistoryQueryResult> value
    ) {
        try {
            String redisKey = toRedisKey(key);
            String redisValue = objectMapper.writeValueAsString(value);
            Duration ttl = Duration.ofSeconds(notificationHistoryCacheProperties.getTtlSeconds());

            stringRedisTemplate.opsForValue().set(redisKey, redisValue, ttl);
            return true;
        } catch (Exception e) {
            log.warn("[CACHE PUT FAIL] notification history cache put failed. key={}", key, e);
            return false;
        }
    }

    private String toRedisKey(NotificationHistoryCacheKey key) {
        return KEY_PREFIX
                + ":requesterId=" + key.requesterId()
                + ":cursorCreatedAt=" + key.cursorCreatedAt()
                + ":cursorId=" + key.cursorId()
                + ":size=" + key.size();
    }

    private JavaType cacheValueType() {
        return objectMapper.getTypeFactory().constructParametricType(
                NotificationCursorResult.class,
                NotificationHistoryQueryResult.class
        );
    }
}
