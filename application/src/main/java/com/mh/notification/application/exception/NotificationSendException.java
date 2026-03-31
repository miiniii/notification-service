package com.mh.notification.application.exception;

import com.mh.notification.domain.FailureType;
import lombok.Getter;

@Getter
public class NotificationSendException extends RuntimeException {

    private final FailureType failureType;
    private final Integer failureStatusCode;

    public NotificationSendException(FailureType failureType, Integer failureStatusCode, String message, Throwable cause) {
        super(message, cause);
        this.failureType = failureType;
        this.failureStatusCode = failureStatusCode;
    }

    public NotificationSendException(FailureType failureType, Integer failureStatusCode, String message) {
        super(message);
        this.failureType = failureType;
        this.failureStatusCode = failureStatusCode;
    }

}
