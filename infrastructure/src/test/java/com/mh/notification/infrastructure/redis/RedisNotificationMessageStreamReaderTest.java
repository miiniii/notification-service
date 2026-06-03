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
class RedisNotificationMessageStreamReaderTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private StreamOperations<String, Object, Object> streamOperations;

    @Mock
    private NotificationConsumerIdentityProvider consumerIdentityProvider;

    @Mock
    private NotificationStreamGroupInitializer notificationStreamGroupInitializer;

    private RedisNotificationMessageStreamReader reader;

    @BeforeEach
    void setUp() {
        reader = new RedisNotificationMessageStreamReader(
                stringRedisTemplate,
                consumerIdentityProvider,
                notificationStreamGroupInitializer
        );
        given(stringRedisTemplate.opsForStream()).willReturn(streamOperations);
        lenient().when(consumerIdentityProvider.getConsumerName()).thenReturn("consumer-1");
    }

    @Test
    void readMessages_whenNoGroup_thenInitializeAndReturnEmptyList() {
        given(streamOperations.read(any(Consumer.class), any(StreamReadOptions.class), any(StreamOffset.class)))
                .willThrow(new RedisSystemException("NOGROUP No such key or consumer group", null));

        List<StreamMessage> messages = reader.readMessages();

        assertThat(messages).isEmpty();
        verify(notificationStreamGroupInitializer).initialize();
    }

    @Test
    void readMessages_whenUnexpectedRedisException_thenThrow() {
        given(streamOperations.read(any(Consumer.class), any(StreamReadOptions.class), any(StreamOffset.class)))
                .willThrow(new RedisSystemException("connection failed", null));

        assertThatThrownBy(() -> reader.readMessages())
                .isInstanceOf(RedisSystemException.class);
    }

    @Test
    void readMessages_whenRecordsExist_thenMapToStreamMessagesAndSkipBootstrap() {
        MapRecord<String, Object, Object> messageRecord = MapRecord
                .create(NotificationStreamKeys.WORK, Map.<Object, Object>of("payload", "payload-json"))
                .withId(RecordId.of("1-0"));
        MapRecord<String, Object, Object> bootstrapRecord = MapRecord
                .create(NotificationStreamKeys.WORK, Map.<Object, Object>of("type", "bootstrap"))
                .withId(RecordId.of("2-0"));

        given(streamOperations.read(any(Consumer.class), any(StreamReadOptions.class), any(StreamOffset.class)))
                .willReturn(List.of(messageRecord, bootstrapRecord));

        List<StreamMessage> messages = reader.readMessages();

        assertThat(messages).containsExactly(new StreamMessage("1-0", "payload-json", 1L));
    }

    @Test
    void reclaimPendingMessages_whenPendingNoGroup_thenInitializeAndReturnEmptyList() {
        given(streamOperations.pending(
                eq(NotificationStreamKeys.WORK),
                eq("notification-group"),
                any(Range.class),
                eq(10L)
        )).willThrow(new RedisSystemException("NOGROUP No such key or consumer group", null));

        List<StreamMessage> messages = reader.reclaimPendingMessages();

        assertThat(messages).isEmpty();
        verify(notificationStreamGroupInitializer).initialize();
    }

    @Test
    void reclaimPendingMessages_whenClaimNoGroup_thenInitializeAndReturnEmptyList() {
        PendingMessage pendingMessage = new PendingMessage(
                RecordId.of("1-0"),
                Consumer.from("notification-group", "consumer-1"),
                Duration.ofSeconds(60),
                2L
        );
        given(streamOperations.pending(
                eq(NotificationStreamKeys.WORK),
                eq("notification-group"),
                any(Range.class),
                eq(10L)
        )).willReturn(new PendingMessages("notification-group", List.of(pendingMessage)));
        given(streamOperations.claim(
                eq(NotificationStreamKeys.WORK),
                eq("notification-group"),
                eq("consumer-1"),
                eq(Duration.ofSeconds(30)),
                any(RecordId.class)
        )).willThrow(new RedisSystemException("NOGROUP No such key or consumer group", null));

        List<StreamMessage> messages = reader.reclaimPendingMessages();

        assertThat(messages).isEmpty();
        verify(notificationStreamGroupInitializer).initialize();
    }
}
