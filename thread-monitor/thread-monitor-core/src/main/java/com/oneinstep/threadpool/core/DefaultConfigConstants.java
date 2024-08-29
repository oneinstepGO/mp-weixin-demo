package com.oneinstep.threadpool.core;

/**
 * 默认线程池配置常量
 */
public class DefaultConfigConstants {

    private DefaultConfigConstants() {
    }

    // 默认的滑动窗口大小
    public static final int DEFAULT_BUCKET_SIZE = 10;

    // 默认的时间窗口大小
    public static final long DEFAULT_TIME_WINDOW_SECONDS = 3;

    // 默认的最大线程数
    public static final int DEFAULT_MAX_POOL_SIZE = 1;

    // 默认的核心线程数
    public static final int DEFAULT_CORE_POOL_SIZE = Runtime.getRuntime().availableProcessors() * 2;

    // 默认的线程存活时间
    public static final long DEFAULT_KEEP_ALIVE_TIME_MS = 60000;

    // 默认的队列容量
    public static final int DEFAULT_QUEUE_CAPACITY = 512;

    // 默认开启监控
    public static final boolean DEFAULT_ENABLE_MONITOR = true;

    // 默认的队列类型
    public static final String DEFAULT_QUEUE_TYPE = "LinkedBlockingQueue";

    // 默认的拒绝策略
    public static final String DEFAULT_POLICY = "CallerRunsPolicy";

}
