package com.mh.notification.infrastructure.redis;

import com.mh.notification.application.dto.StreamMessage;
import com.mh.notification.application.port.NotificationMessageStreamReader;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class RedisNotificationMessageStreamReader implements NotificationMessageStreamReader {

    private static final String STREAM_KEY = NotificationStreamKeys.WORK;
    private static final String GROUP_NAME = "notification-group";

    private final StringRedisTemplate stringRedisTemplate;
    private final NotificationConsumerIdentityProvider consumerIdentityProvider;

    /**
     * 최대 10개 읽고, 없으면 2초 대기
     */
    @Override
    public List<StreamMessage> readMessages() {
        List<MapRecord<String, Object, Object>> records = stringRedisTemplate.opsForStream()
                                                        .read(
                                                                Consumer.from(GROUP_NAME, consumerIdentityProvider.getConsumerName()),
                                                                StreamReadOptions.empty()
                                                                        .count(10)
                                                                        .block(Duration.ofSeconds(2)),
                                                                StreamOffset.create(STREAM_KEY, ReadOffset.lastConsumed())
                                                        );
        List<StreamMessage> messages = new ArrayList<>();

        if (records == null || records.isEmpty()) return messages;

        for (MapRecord<String, Object, Object> record : records) {
            Map<Object, Object> value = record.getValue();
            String payload = String.valueOf(value.get("payload"));
            messages.add(StreamMessage.of(record.getId().getValue(), payload));
        }

        return messages;
    }
}
