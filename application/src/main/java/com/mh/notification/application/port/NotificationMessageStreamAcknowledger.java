package com.mh.notification.application.port;

public interface NotificationMessageStreamAcknowledger {

    void ack(String recordId);
}
