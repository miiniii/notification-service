package com.mh.notification.application.dto;

public record StreamMessage (
        String recordId,
        String payload,
        long deliveryCount

) {
    public static StreamMessage of(String recordId, String payload) {
        return new StreamMessage(recordId, payload, 1L);
    }

    public static StreamMessage of(String recordId, String payload, long deliveryCount) {
        return new StreamMessage(recordId, payload, deliveryCount);
    }
}
