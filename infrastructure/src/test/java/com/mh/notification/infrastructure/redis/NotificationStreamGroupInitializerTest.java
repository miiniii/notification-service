package com.mh.notification.infrastructure.redis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamInfo;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationStreamGroupInitializerTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private StreamOperations<String, Object, Object> streamOperations;

    @Mock
    private RedisConnection redisConnection;

    private NotificationStreamGroupInitializer initializer;

    @BeforeEach
    void setUp() {
        initializer = new NotificationStreamGroupInitializer(stringRedisTemplate);
        lenient().when(stringRedisTemplate.opsForStream()).thenReturn(streamOperations);
    }

    @Test
    void initialize_whenGroupExists_thenDoNothing() {
        StreamInfo.XInfoGroups groups = mock(StreamInfo.XInfoGroups.class);
        StreamInfo.XInfoGroup group = mock(StreamInfo.XInfoGroup.class);
        given(streamOperations.groups(NotificationStreamKeys.WORK)).willReturn(groups);
        given(groups.stream()).willReturn(java.util.stream.Stream.of(group));
        given(group.groupName()).willReturn("notification-group");

        initializer.initialize();

        verify(stringRedisTemplate, never()).execute(any(RedisCallback.class));
    }

    @Test
    void initialize_whenGroupMissing_thenCreateGroupWithMkStream() {
        StreamInfo.XInfoGroups groups = mock(StreamInfo.XInfoGroups.class);
        given(streamOperations.groups(NotificationStreamKeys.WORK)).willReturn(groups);
        given(groups.stream()).willReturn(java.util.stream.Stream.empty());
        given(stringRedisTemplate.execute(any(RedisCallback.class))).willAnswer(invocation -> {
            RedisCallback<?> callback = invocation.getArgument(0);
            return callback.doInRedis(redisConnection);
        });

        initializer.initialize();

        verify(redisConnection).xGroupCreate(
                eq(NotificationStreamKeys.WORK.getBytes(StandardCharsets.UTF_8)),
                eq("notification-group"),
                any(ReadOffset.class),
                eq(true)
        );
    }

    @Test
    void initialize_whenXinfoNoSuchKey_thenCreateGroupWithMkStream() {
        given(streamOperations.groups(NotificationStreamKeys.WORK))
                .willThrow(new RedisSystemException("ERR no such key", null));
        given(stringRedisTemplate.execute(any(RedisCallback.class))).willAnswer(invocation -> {
            RedisCallback<?> callback = invocation.getArgument(0);
            return callback.doInRedis(redisConnection);
        });

        initializer.initialize();

        verify(redisConnection).xGroupCreate(
                eq(NotificationStreamKeys.WORK.getBytes(StandardCharsets.UTF_8)),
                eq("notification-group"),
                any(ReadOffset.class),
                eq(true)
        );
    }

    @Test
    void initialize_whenBusyGroup_thenDoesNotThrow() {
        StreamInfo.XInfoGroups groups = mock(StreamInfo.XInfoGroups.class);
        given(streamOperations.groups(NotificationStreamKeys.WORK)).willReturn(groups);
        given(groups.stream()).willReturn(java.util.stream.Stream.empty());
        given(stringRedisTemplate.execute(any(RedisCallback.class)))
                .willThrow(new RedisSystemException("BUSYGROUP Consumer Group name already exists", null));

        assertThatCode(() -> initializer.initialize()).doesNotThrowAnyException();
    }

    @Test
    void initialize_whenUnexpectedRedisException_thenDoesNotThrow() {
        given(streamOperations.groups(NotificationStreamKeys.WORK))
                .willThrow(new RedisSystemException("connection failed", null));

        assertThatCode(() -> initializer.initialize()).doesNotThrowAnyException();
    }
}
