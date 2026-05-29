package com.mh.notification.infrastructure.redis;

import com.mh.notification.application.dto.StreamMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RedisNotificationPendingMessageReclaimerTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private StreamOperations<String, Object, Object> streamOperations;

    @Mock
    private NotificationConsumerIdentityProvider consumerIdentityProvider;

    @Mock
    private NotificationStreamGroupInitializer notificationStreamGroupInitializer;

    private RedisNotificationPendingMessageReclaimer reclaimer;

    @BeforeEach
    void setUp() {
        reclaimer = new RedisNotificationPendingMessageReclaimer(
                stringRedisTemplate,
                consumerIdentityProvider,
                notificationStreamGroupInitializer
        );
        given(stringRedisTemplate.opsForStream()).willReturn(streamOperations);
        lenient().when(consumerIdentityProvider.getConsumerName()).thenReturn("consumer-1");
    }

    @Test
    void reclaimPendingMessages_whenPendingNoGroup_thenInitializeAndReturnEmptyList() {
        given(streamOperations.pending(
                eq(NotificationStreamKeys.WORK),
                eq("notification-group"),
                any(Range.class),
                eq(10L),
                eq(Duration.ofSeconds(5))
        )).willThrow(new RedisSystemException("NOGROUP No such key or consumer group", null));

        List<StreamMessage> messages = reclaimer.reclaimPendingMessages();

        assertThat(messages).isEmpty();
        verify(notificationStreamGroupInitializer).initialize();
    }

    @Test
    void reclaimPendingMessages_whenPendingUnexpectedException_thenThrow() {
        given(streamOperations.pending(
                eq(NotificationStreamKeys.WORK),
                eq("notification-group"),
                any(Range.class),
                eq(10L),
                eq(Duration.ofSeconds(5))
        )).willThrow(new RedisSystemException("connection failed", null));

        assertThatThrownBy(() -> reclaimer.reclaimPendingMessages())
                .isInstanceOf(RedisSystemException.class);
    }

    @Test
    void reclaimPendingMessages_whenClaimNoGroup_thenInitializeAndReturnEmptyList() {
        PendingMessage pendingMessage = pendingMessage();
        given(streamOperations.pending(
                eq(NotificationStreamKeys.WORK),
                eq("notification-group"),
                any(Range.class),
                eq(10L),
                eq(Duration.ofSeconds(5))
        )).willReturn(new PendingMessages("notification-group", List.of(pendingMessage)));
        given(streamOperations.claim(
                eq(NotificationStreamKeys.WORK),
                eq("notification-group"),
                eq("consumer-1"),
                eq(Duration.ofSeconds(5)),
                any(RecordId[].class)
        )).willThrow(new RedisSystemException("NOGROUP No such key or consumer group", null));

        List<StreamMessage> messages = reclaimer.reclaimPendingMessages();

        assertThat(messages).isEmpty();
        verify(notificationStreamGroupInitializer).initialize();
    }

    @Test
    void reclaimPendingMessages_whenClaimedRecordsExist_thenMapToStreamMessagesAndSkipBootstrap() {
        PendingMessage pendingMessage = pendingMessage();
        MapRecord<String, Object, Object> messageRecord = MapRecord
                .create(NotificationStreamKeys.WORK, Map.<Object, Object>of("payload", "payload-json"))
                .withId(RecordId.of("1-0"));
        MapRecord<String, Object, Object> bootstrapRecord = MapRecord
                .create(NotificationStreamKeys.WORK, Map.<Object, Object>of("type", "bootstrap"))
                .withId(RecordId.of("2-0"));

        given(streamOperations.pending(
                eq(NotificationStreamKeys.WORK),
                eq("notification-group"),
                any(Range.class),
                eq(10L),
                eq(Duration.ofSeconds(5))
        )).willReturn(new PendingMessages("notification-group", List.of(pendingMessage)));
        given(streamOperations.claim(
                eq(NotificationStreamKeys.WORK),
                eq("notification-group"),
                eq("consumer-1"),
                eq(Duration.ofSeconds(5)),
                any(RecordId[].class)
        )).willReturn(List.of(messageRecord, bootstrapRecord));

        List<StreamMessage> messages = reclaimer.reclaimPendingMessages();

        assertThat(messages).containsExactly(new StreamMessage("1-0", "payload-json", 1L));
    }

    private PendingMessage pendingMessage() {
        return new PendingMessage(
                RecordId.of("1-0"),
                Consumer.from("notification-group", "consumer-1"),
                Duration.ofSeconds(60),
                2L
        );
    }
}
