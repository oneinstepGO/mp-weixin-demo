package com.oneinstep.myspi0.demo;

import com.oneinstep.myspi0.core.URL;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 注册工厂基类
 * 处理异常和线程安全
 */
@Slf4j
public abstract class AbstractRegistryFactory implements RegistryFactory {

    private static final Lock LOCK = new ReentrantLock();

    @Override
    public Registry getRegistry(URL url) {

        Registry registry;

        // 加锁，保证注册中心访问过程中只有一个实例
        LOCK.lock();
        try {

            // 通过spi/ioc创建注册中心
            registry = createRegistry(url);
            if (registry == null) {
                throw new IllegalStateException("Can not create registry " + url);
            }

        } catch (Exception e) {
            throw new RuntimeException("Can not create registry " + url, e);
        } finally {
            // 释放锁
            LOCK.unlock();
        }

        return registry;
    }

    /**
     * 创建注册中心，由子类实现
     *
     * @param url registry url
     * @return registry
     */
    protected abstract Registry createRegistry(URL url);
}
