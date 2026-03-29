package com.mh.notification.application.dto;

public record StreamMessage (
        String recordId,
        String payload
) {
    public static StreamMessage of(String recordId, String payload) {
        return new StreamMessage(recordId, payload);
    }
}
