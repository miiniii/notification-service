package com.mh.notification.application.port;

import com.mh.notification.application.dto.ArchiveNotificationRow;

import java.util.List;

public interface ArchiveNotificationRepository {

    void saveAll(List<ArchiveNotificationRow> rows);
}
