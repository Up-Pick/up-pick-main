package org.oneog.uppick.auction.common.lock;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LockManager {

    private final RedissonClient redissonClient;
    private static final int MAX_WAIT_SECONDS = 30;

    public <T> T executeWithLock(String lockKey, Supplier<T> criticalSection) {

        var lock = redissonClient.getLock(lockKey);
        try {

            boolean isLocked = false;
            try {
                isLocked = lock.tryLock(MAX_WAIT_SECONDS, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new LockAcquisitionException();
            }
            if (!isLocked) {
                throw new LockAcquisitionException();
            }
            return criticalSection.get();
        } catch (Exception e) {
            throw e;
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    public void executeWithLock(String lockKey, Runnable criticalSection) {

        executeWithLock(lockKey, () -> {
            criticalSection.run();
            return null;
        });
    }

}
