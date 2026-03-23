package com.mh.notification.infrastructure.redis;

import com.mh.notification.application.dto.StreamMessage;
import com.mh.notification.application.port.WaitMessageStreamReader;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class RedisWaitMessageStreamReader implements WaitMessageStreamReader {

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public List<StreamMessage> readMessages() {
        List<MapRecord<String, Object, Object>> records = stringRedisTemplate.opsForStream()
                .range(NotificationStreamKeys.WAIT, Range.unbounded());

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
