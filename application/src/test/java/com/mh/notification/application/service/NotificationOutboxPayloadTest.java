package com.mh.notification.application.service;

import com.mh.notification.application.dto.NotificationOutboxPayload;
import com.mh.notification.domain.Notification;
import com.mh.notification.domain.NotificationChannel;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationOutboxPayloadTest {

    @Test
    void from_mapsNotificationAndReceiver() throws Exception {
        // given
        Notification notification = Notification.create(
                "req-123456789",
                1001L,
                2001L,
                "PAYMENT",
                NotificationChannel.SMS,
                "결제 완료",
                "결제가 정상적으로 완료되었습니다.",
                "/payments/123"
        );

        setField(notification, "id", 1L);

        String receiver = "01012345678";

        // when
        NotificationOutboxPayload payload = NotificationOutboxPayload.from(notification, receiver);

        // then
        assertThat(payload.notificationId()).isEqualTo(1L);
        assertThat(payload.requestId()).isEqualTo("req-123456789");
        assertThat(payload.requesterId()).isEqualTo(1001L);
        assertThat(payload.userId()).isEqualTo(2001L);
        assertThat(payload.service()).isEqualTo("PAYMENT");
        assertThat(payload.channel()).isEqualTo(NotificationChannel.SMS);
        assertThat(payload.title()).isEqualTo("결제 완료");
        assertThat(payload.body()).isEqualTo("결제가 정상적으로 완료되었습니다.");
        assertThat(payload.targetUrl()).isEqualTo("/payments/123");
        assertThat(payload.receiver()).isEqualTo("01012345678");
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}