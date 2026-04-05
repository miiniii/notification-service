package com.mh.notification.application.dto;

public enum ConsumeResult {
    SUCCESS,
    ALREADY_PROCESSED,
    LOCK_FAILED,
    RETRYABLE_FAIL,
    DEAD_LETTERED
}
