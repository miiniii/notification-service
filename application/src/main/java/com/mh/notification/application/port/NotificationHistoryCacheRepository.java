package com.mh.notification.application.port;

import com.mh.notification.application.dto.NotificationCursorResult;
import com.mh.notification.application.dto.NotificationHistoryCacheKey;
import com.mh.notification.application.dto.NotificationHistoryCacheLookup;
import com.mh.notification.application.dto.NotificationHistoryQueryResult;

public interface NotificationHistoryCacheRepository {

    NotificationHistoryCacheLookup get(NotificationHistoryCacheKey key);

    boolean put(
            NotificationHistoryCacheKey key,
            NotificationCursorResult<NotificationHistoryQueryResult> value
    );
}
