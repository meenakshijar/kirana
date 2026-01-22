package com.example.kirana.lock;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedissonLockService {

    private final RedissonClient redissonClient;

    public RedissonLockService(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    public RLock lock(String key, long waitSeconds, long leaseSeconds) {
        try {
            RLock lock = redissonClient.getLock(key);

            boolean acquired = lock.tryLock(waitSeconds, leaseSeconds, TimeUnit.SECONDS);

            if (!acquired) {
                return null;
            }

            return lock;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Lock interrupted");
        }
    }

    public void unlock(RLock lock) {
        if (lock != null && lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }
}
