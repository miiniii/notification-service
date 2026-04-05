package com.mh.notification.infrastructure.redis;

import com.mh.notification.application.dto.StreamMessage;
import com.mh.notification.application.port.NotificationPendingMessageReclaimer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.PendingMessage;
import org.springframework.data.redis.connection.stream.PendingMessages;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class RedisNotificationPendingMessageReclaimer implements NotificationPendingMessageReclaimer {

    /**
     * 5초 이상 pending 상태인 메시지만 대상(한번에 최대 10개)
     */
    private static final String STREAM_KEY = NotificationStreamKeys.WORK;
    private static final String GROUP_NAME = "notification-group";
    private static final Duration MIN_IDLE_TIME = Duration.ofSeconds(5);
    private static final long FETCH_COUNT = 10L;

    private final StringRedisTemplate stringRedisTemplate;
    private final NotificationConsumerIdentityProvider consumerIdentityProvider;


    @Override
    public List<StreamMessage> reclaimPendingMessages() {
        // 1) idle time이 일정 이상 지난 pending 메시지 조회
        PendingMessages pendingMessages = stringRedisTemplate.opsForStream()
                                .pending(STREAM_KEY, GROUP_NAME, Range.unbounded(), FETCH_COUNT, MIN_IDLE_TIME);

        if (pendingMessages == null || pendingMessages.isEmpty()) {
            return List.of();
        }

        // 2) reclaim 대상 recordId 수집
        List<RecordId> recordIds = new ArrayList<>();
        for (PendingMessage pendingMessage : pendingMessages) {
            recordIds.add(pendingMessage.getId());
        }

        // 3) 현재 consumer가 XCLAIM
        List<MapRecord<String, Object, Object>> claimedRecords = stringRedisTemplate.opsForStream()
                .claim(
                        STREAM_KEY, GROUP_NAME, consumerIdentityProvider.getConsumerName(), MIN_IDLE_TIME, recordIds.toArray(new RecordId[0])
                );

        if (claimedRecords == null || claimedRecords.isEmpty()) {
            return List.of();
        }

        // 4) StreamMessage로 변환
        List<StreamMessage> result = new ArrayList<>();
        for (MapRecord<String, Object, Object> record : claimedRecords) {
            Map<Object, Object> value = record.getValue();
            String payload = String.valueOf(value.get("payload"));
            result.add(StreamMessage.of(record.getId().getValue(),payload));
        }

        return result;
    }

}
