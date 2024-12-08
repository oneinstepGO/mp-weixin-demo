package com.oneinstep.ddd.asset.util;

import cn.hutool.core.lang.Pair;
import jakarta.annotation.Resource;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

/**
 * 分布式锁
 */
@Component
public class DistributedLock {

    @Resource
    private RedissonClient redissonClient;

    /**
     * 加锁
     *
     * @param lockKey  锁的key
     * @param supplier 获取锁后执行的逻辑
     * @return 获取锁后执行的逻辑返回值
     */
    public <T> T lock(String lockKey, Supplier<T> supplier) {
        RLock lock = redissonClient.getLock(lockKey);
        lock.lock();
        try {
            return supplier.get();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 尝试加锁
     *
     * @param lockKey  锁的key
     * @param supplier 获取锁后执行的逻辑
     * @return 获取锁后执行的逻辑返回值
     */
    public <T> Pair<Boolean, T> tryLock(String lockKey, Supplier<T> supplier) {
        RLock lock = redissonClient.getLock(lockKey);
        boolean success = lock.tryLock();
        if (success) {
            try {
                return Pair.of(true, supplier.get());
            } finally {
                lock.unlock();
            }
        }
        return Pair.of(false, null);
    }
}
