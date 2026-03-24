package com.mh.notification.infrastructure.redis;

import com.mh.notification.application.port.NotificationDistributedLockManager;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedissonNotificationDistributedLockManager implements NotificationDistributedLockManager {

    private final RedissonClient redissonClient;

    @Override
    public boolean tryLock(String key, long waitTimeMillis, long leaseTimeMillis) {
        RLock lock = redissonClient.getLock(key);

        try {
            return lock.tryLock(waitTimeMillis, leaseTimeMillis, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    @Override
    public void unlock(String key) {
        RLock lock = redissonClient.getLock(key);

        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }
}
