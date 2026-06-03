package com.mh.notification.infrastructure.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mh.notification.application.dto.NotificationCursorResult;
import com.mh.notification.application.dto.NotificationHistoryCacheKey;
import com.mh.notification.application.dto.NotificationHistoryCacheLookup;
import com.mh.notification.application.dto.NotificationHistoryCacheLookupStatus;
import com.mh.notification.application.dto.NotificationHistoryQueryResult;
import com.mh.notification.domain.NotificationChannel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RedisNotificationHistoryCacheRepositoryTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private ObjectMapper objectMapper;
    private NotificationHistoryCacheProperties properties;
    private RedisNotificationHistoryCacheRepository repository;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        properties = new NotificationHistoryCacheProperties();
        properties.setTtlSeconds(60);

        repository = new RedisNotificationHistoryCacheRepository(
                stringRedisTemplate,
                objectMapper,
                properties
        );

        lenient().when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void get_whenRedisValueExists_thenDeserializeAndReturnHit() throws Exception {
        NotificationHistoryCacheKey key = cacheKey();
        NotificationCursorResult<NotificationHistoryQueryResult> expected = result();
        given(valueOperations.get(redisKey())).willReturn(objectMapper.writeValueAsString(expected));

        NotificationHistoryCacheLookup lookup = repository.get(key);

        assertThat(lookup.status()).isEqualTo(NotificationHistoryCacheLookupStatus.HIT);
        assertThat(lookup.value()).isEqualTo(expected);
    }

    @Test
    void get_whenRedisValueMissing_thenReturnMiss() {
        given(valueOperations.get(redisKey())).willReturn(null);

        NotificationHistoryCacheLookup lookup = repository.get(cacheKey());

        assertThat(lookup.status()).isEqualTo(NotificationHistoryCacheLookupStatus.MISS);
        assertThat(lookup.value()).isNull();
    }

    @Test
    void get_whenRedisThrowsException_thenReturnError() {
        given(valueOperations.get(redisKey())).willThrow(new RuntimeException("redis down"));

        NotificationHistoryCacheLookup lookup = repository.get(cacheKey());

        assertThat(lookup.status()).isEqualTo(NotificationHistoryCacheLookupStatus.ERROR);
        assertThat(lookup.value()).isNull();
    }

    @Test
    void get_whenDeserializeFails_thenReturnError() {
        given(valueOperations.get(redisKey())).willReturn("{");

        NotificationHistoryCacheLookup lookup = repository.get(cacheKey());

        assertThat(lookup.status()).isEqualTo(NotificationHistoryCacheLookupStatus.ERROR);
        assertThat(lookup.value()).isNull();
    }

    @Test
    void put_whenSerializeSucceeds_thenSetWithTtl() {
        boolean stored = repository.put(cacheKey(), result());

        assertThat(stored).isTrue();
        verify(valueOperations).set(eq(redisKey()), anyString(), eq(Duration.ofSeconds(60)));
    }

    @Test
    void put_whenRedisThrowsException_thenReturnFalse() {
        doThrow(new RuntimeException("redis down"))
                .when(valueOperations)
                .set(eq(redisKey()), anyString(), eq(Duration.ofSeconds(60)));

        boolean stored = repository.put(cacheKey(), result());

        assertThat(stored).isFalse();
    }

    @Test
    void put_whenSerializeFails_thenReturnFalse() throws Exception {
        ObjectMapper failingObjectMapper = org.mockito.Mockito.mock(ObjectMapper.class);
        given(failingObjectMapper.writeValueAsString(any())).willThrow(new RuntimeException("serialize fail"));

        RedisNotificationHistoryCacheRepository failingRepository =
                new RedisNotificationHistoryCacheRepository(stringRedisTemplate, failingObjectMapper, properties);

        boolean stored = failingRepository.put(cacheKey(), result());

        assertThat(stored).isFalse();
    }

    private NotificationHistoryCacheKey cacheKey() {
        return new NotificationHistoryCacheKey(
                1L,
                LocalDateTime.of(2026, 5, 28, 10, 0),
                10L,
                20
        );
    }

    private String redisKey() {
        return "notification:history"
                + ":requesterId=1"
                + ":cursorCreatedAt=2026-05-28T10:00"
                + ":cursorId=10"
                + ":size=20";
    }

    private NotificationCursorResult<NotificationHistoryQueryResult> result() {
        NotificationHistoryQueryResult item = new NotificationHistoryQueryResult(
                10L,
                "req-123",
                1L,
                2L,
                "PAYMENT",
                NotificationChannel.EMAIL,
                "title",
                "body",
                "/target",
                false,
                LocalDateTime.of(2026, 5, 28, 9, 0),
                List.of()
        );

        return new NotificationCursorResult<>(
                List.of(item),
                false,
                item.createdAt(),
                item.notificationId()
        );
    }
}
