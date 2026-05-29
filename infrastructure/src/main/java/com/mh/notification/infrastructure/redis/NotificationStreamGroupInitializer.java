package com.mh.notification.infrastructure.redis;


import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamInfo;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationStreamGroupInitializer {

    private static final String STREAM_KEY = NotificationStreamKeys.WORK;
    private static final String GROUP_NAME = "notification-group";

    private final StringRedisTemplate stringRedisTemplate;

    @PostConstruct
    public void init() {
        initialize();
    }

    public void initialize() {
        try {
            if (consumerGroupExists()) {
                return;
            }

            createGroupWithMkStream();

            log.info("stream group created. stream={}, group={}", STREAM_KEY, GROUP_NAME);
        } catch (Exception e) {
            if (RedisStreamExceptionUtils.isBusyGroup(e)) {
                log.info("stream group already exists. stream={}, group={}", STREAM_KEY, GROUP_NAME);
                return;
            }

            log.warn("stream group initialization failed. stream={}, group={}, message={}",
                    STREAM_KEY, GROUP_NAME, e.getMessage());
        }
    }

    private boolean consumerGroupExists() {
        try {
            StreamInfo.XInfoGroups groups = stringRedisTemplate.opsForStream().groups(STREAM_KEY);
            return groups.stream()
                    .anyMatch(group -> GROUP_NAME.equals(group.groupName()));
        } catch (Exception e) {
            if (RedisStreamExceptionUtils.isNoSuchStream(e)) {
                return false;
            }
            throw e;
        }
    }

    private void createGroupWithMkStream() {
        stringRedisTemplate.execute((RedisCallback<Void>) connection -> {
            connection.xGroupCreate(
                    STREAM_KEY.getBytes(StandardCharsets.UTF_8),
                    GROUP_NAME,
                    ReadOffset.latest(),
                    true
            );
            return null;
        });
    }
}
