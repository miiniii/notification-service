package com.mh.notification.mock.api;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Locale;

public enum ChannelType {
    KAKAO,
    SMS,
    EMAIL,
    UNKNOWN;

    @JsonCreator
    public static ChannelType from(String value) {
        if (value == null || value.isBlank()) {
            return UNKNOWN;
        }

        try {
            return ChannelType.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }
}