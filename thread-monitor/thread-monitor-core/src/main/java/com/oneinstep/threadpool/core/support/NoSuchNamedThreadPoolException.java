package com.oneinstep.threadpool.core.support;

/**
 * 线程池不存在异常
 */
public class NoSuchNamedThreadPoolException extends Exception {
    public NoSuchNamedThreadPoolException(String message) {
        super(message);
    }
}
