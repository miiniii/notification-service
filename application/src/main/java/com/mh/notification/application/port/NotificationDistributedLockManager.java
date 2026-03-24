package com.mh.notification.application.port;

public interface NotificationDistributedLockManager {

    boolean tryLock(String key, long waitTimeMillis, long leaseTimeMillis);
    void unlock(String key);
}
