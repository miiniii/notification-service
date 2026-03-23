package com.mh.notification.application.port;

public interface NotificationMessageStreamDeleter {
    void delete(String recordId);
}
