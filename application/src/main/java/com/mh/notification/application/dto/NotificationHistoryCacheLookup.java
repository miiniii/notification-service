package com.mh.notification.application.dto;

public record NotificationHistoryCacheLookup(
        NotificationHistoryCacheLookupStatus status,
        NotificationCursorResult<NotificationHistoryQueryResult> value
) {

    public static NotificationHistoryCacheLookup hit(NotificationCursorResult<NotificationHistoryQueryResult> value) {
        return new NotificationHistoryCacheLookup(NotificationHistoryCacheLookupStatus.HIT, value);
    }

    public static NotificationHistoryCacheLookup miss() {
        return new NotificationHistoryCacheLookup(NotificationHistoryCacheLookupStatus.MISS, null);
    }

    public static NotificationHistoryCacheLookup error() {
        return new NotificationHistoryCacheLookup(NotificationHistoryCacheLookupStatus.ERROR, null);
    }
}
