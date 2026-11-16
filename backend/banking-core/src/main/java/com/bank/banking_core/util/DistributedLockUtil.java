package com.bank.banking_core.util;

import com.bank.banking_core.constants.AppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Slf4j
@Component
@RequiredArgsConstructor
public class DistributedLockUtil {

    private final RedissonClient redissonClient;

    public <T> T executeWithLock(String lockKey,
                                 long waitTimeSeconds,
                                 long leaseTimeSeconds,
                                 Supplier<T> task) {
        RLock lock = redissonClient.getLock(lockKey);
        boolean isLocked = false;
        try {
            log.info("Attempting to acquire distributed lock for key: {}", lockKey);
            isLocked = lock.tryLock(waitTimeSeconds, leaseTimeSeconds, TimeUnit.SECONDS);
            if (!isLocked) {
                log.warn("Could not acquire distributed lock for key: {}", lockKey);
                throw new IllegalStateException("System is currently processing another transaction for this account. Please try again.");
            }
            log.info("Lock acquired successfully for key: {}", lockKey);
            return task.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Thread interrupted while waiting for distributed lock on: " + lockKey, e);
        } finally {
            if (isLocked && lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.info("Lock released for key: {}", lockKey);
            }
        }
    }

    public <T> T executeWithAccountLock(Long accountId, Supplier<T> task) {
        String lockKey = AppConstants.LOCK_ACCOUNT_PREFIX + accountId;
        return executeWithLock(lockKey, AppConstants.LOCK_WAIT_TIME_SECONDS, AppConstants.LOCK_LEASE_TIME_SECONDS, task);
    }
}
