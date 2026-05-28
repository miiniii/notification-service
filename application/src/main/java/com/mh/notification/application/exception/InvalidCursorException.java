package com.mh.notification.application.exception;

public class InvalidCursorException extends RuntimeException {

    public static final String MESSAGE = "cursorCreatedAt and cursorId must be both present or both absent.";

    public InvalidCursorException() {
        super(MESSAGE);
    }
}
