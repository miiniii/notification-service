package com.mh.notification.application.port;

import com.mh.notification.application.dto.StreamMessage;

import java.util.List;

public interface NotificationPendingMessageReclaimer {
    List<StreamMessage> reclaimPendingMessages();
}
