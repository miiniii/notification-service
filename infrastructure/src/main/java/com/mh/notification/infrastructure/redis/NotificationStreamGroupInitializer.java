package com.mh.notification.infrastructure.redis;


import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationStreamGroupInitializer {

    private static final String STREAM_KEY = NotificationStreamKeys.WORK;
    private static final String GROUP_NAME = "notification-group";

    private final StringRedisTemplate stringRedisTemplate;

    @PostConstruct
    public void init() {
        try {
            stringRedisTemplate.opsForStream()
                    .createGroup(STREAM_KEY, ReadOffset.from("0-0"), GROUP_NAME);

            log.info("stream group created. stream={}, group={}", STREAM_KEY, GROUP_NAME);
        } catch (Exception e) {
            log.info("stream group may already exist. stream={}, group={}, message={}",
                    STREAM_KEY, GROUP_NAME, e.getMessage());
        }
    }
}
