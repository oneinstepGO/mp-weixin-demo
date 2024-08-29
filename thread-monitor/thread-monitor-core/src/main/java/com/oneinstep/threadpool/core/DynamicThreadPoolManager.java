package com.oneinstep.threadpool.core;

import com.oneinstep.threadpool.core.support.*;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Stream;

/**
 * 可以动态修改线程池参数的管理器
 */
@Component
@Primary
@Slf4j
public class DynamicThreadPoolManager {

    @Resource
    private ApplicationContext applicationContext;
    @Resource
    private DynamicThreadPoolProperties dynamicThreadPoolProperties;
    @Value("${spring.application.name}")
    private String applicationName;

    // 线程池名称 -> 读写锁
    private static final Map<String, ReentrantReadWriteLock> LOCK_MAP = new HashMap<>();

    /**
     * 修改线程池参数
     *
     * @param newConfig 新的线程池配置
     */
    public synchronized void modifyThreadPool(ThreadPoolConfig newConfig) throws NoSuchNamedThreadPoolException {
        log.info("Modify thread pool: {}", newConfig);
        String poolName = newConfig.getPoolName();
        checkParams(newConfig);
        resetIfChanged(poolName, newConfig);
        log.info("Modify thread pool [{}] success", poolName);
    }

    private void resetIfChanged(String poolName, ThreadPoolConfig newConfig) {

        DynamicThreadPool threadPoolToChange = applicationContext.getBean(poolName, DynamicThreadPool.class);

        ThreadPoolConfig oldConfig = threadPoolToChange.getThreadPoolConfig();
        if (newConfig.getWorkQueue() != null) {
            Integer oldCapacity = oldConfig.getWorkQueue().getCapacity();
            Integer newCapacity = newConfig.getWorkQueue().getCapacity();
            // 如果队列类型不同或者容量不同，需要重新设置队列
            if (!oldConfig.getWorkQueue().getType().equals(newConfig.getWorkQueue().getType()) || !Objects.equals(oldCapacity, newCapacity)) {
                mayUpdateNewConfig(oldConfig, newConfig);
                // 重新设置线程池
                resetThreadPool(poolName, newConfig);
                return;
            }
        }

        // 以下情况不需要重新设置线程池
        // 如果线程池参数有变化，需要重新设置
        // 需要先设置最大线程数，再设置核心线程数
        boolean needChangeMax = false;
        boolean needChangeCore = false;
        boolean needChangeKeepAlive = false;
        boolean needChangeHandler = false;
        boolean needChangeMonitor = false;

        final int oldMax = oldConfig.getMaxPoolSize();
        if (newConfig.getMaxPoolSize() != oldMax) {
            needChangeMax = true;
        }
        final int oldCore = oldConfig.getCorePoolSize();
        if (newConfig.getCorePoolSize() != oldCore) {
            needChangeCore = true;
        }
        long oldKeepAliveTimeMs = oldConfig.getKeepAliveTimeMs();
        if (newConfig.getKeepAliveTimeMs() != null && !Objects.equals(newConfig.getKeepAliveTimeMs(), oldKeepAliveTimeMs)) {
            needChangeKeepAlive = true;
        }
        if (StringUtils.isNotBlank(newConfig.getPolicy()) && !newConfig.getPolicy().equals(oldConfig.getPolicy())) {
            needChangeHandler = true;
        }
        if (newConfig.getMonitor() != null && (!Objects.equals(newConfig.getMonitor().getEnabled(), oldConfig.getMonitor().getEnabled())
                || !Objects.equals(newConfig.getMonitor().getTimeWindowSeconds(), oldConfig.getMonitor().getTimeWindowSeconds())
        || !Objects.equals(newConfig.getMonitor().getMonitorUrl(), oldConfig.getMonitor().getMonitorUrl()))) {
            needChangeMonitor = true;
        }

        if (needChangeMax) {
            threadPoolToChange.setMaximumPoolSize(newConfig.getMaxPoolSize());
        }
        if (needChangeCore) {
            threadPoolToChange.setCorePoolSize(newConfig.getCorePoolSize());
        }
        if (needChangeKeepAlive) {
            threadPoolToChange.setKeepAliveTime(newConfig.getKeepAliveTimeMs(), TimeUnit.MILLISECONDS);
        }
        if (needChangeHandler) {
            threadPoolToChange.setRejectedExecutionHandler(RejectPolicyEnum.createRejectedExecutionHandler(newConfig.getPolicy()));
        }
        if (needChangeMonitor) {
            log.info("Modify thread pool [{}] monitor: {}", poolName, newConfig.getMonitor());
            threadPoolToChange.updateMonitor(newConfig.getMonitor());
        }

    }

    private void mayUpdateNewConfig(ThreadPoolConfig oldConfig, ThreadPoolConfig newConfig) {
        if (newConfig.getMaxPoolSize() == null) {
            newConfig.setMaxPoolSize(oldConfig.getMaxPoolSize());
        }
        if (newConfig.getCorePoolSize() == null) {
            newConfig.setCorePoolSize(oldConfig.getCorePoolSize());
        }
        if (newConfig.getKeepAliveTimeMs() == null) {
            newConfig.setKeepAliveTimeMs(oldConfig.getKeepAliveTimeMs());
        }
        if (newConfig.getWorkQueue() == null) {
            newConfig.setWorkQueue(oldConfig.getWorkQueue());
        }
        if (StringUtils.isBlank(newConfig.getPolicy())) {
            newConfig.setPolicy(oldConfig.getPolicy());
        }
        if (newConfig.getMonitor() == null) {
            newConfig.setMonitor(oldConfig.getMonitor());
        }
    }

    private void checkParams(ThreadPoolConfig newConfig) throws NoSuchNamedThreadPoolException {
        check(newConfig);
        checkQueue(newConfig);
    }

    private static void checkQueue(ThreadPoolConfig newConfig) {
        WorkQueueConfig workQueue = newConfig.getWorkQueue();
        String poolName = newConfig.getPoolName();
        if (workQueue != null && StringUtils.isNotBlank(workQueue.getType())) {
            String queueType = workQueue.getType();
            BlockingQueueEnum queueEnum = BlockingQueueEnum.getQueueByName(queueType);
            Integer queueCapacity = workQueue.getCapacity();
            if (queueCapacity != null && queueCapacity < 0) {
                log.error("Modify thread pool [{}] failed, queue capacity < 0", poolName);
                throw new IllegalArgumentException("queue capacity < 0");
            }

            if (BlockingQueueEnum.SYNCHRONOUS_QUEUE.equals(queueEnum)) {
                newConfig.getWorkQueue().setCapacity(null);
            } else if (queueCapacity == null) {
                log.error("Modify thread pool [{}] failed, queue capacity is null", poolName);
                throw new IllegalArgumentException("queue capacity is null");
            }
        }
    }

    private void check(ThreadPoolConfig newConfig) throws NoSuchNamedThreadPoolException {
        String poolName = newConfig.getPoolName();
        if (StringUtils.isBlank(poolName) || isThreadPoolNotExist(poolName)) {
            log.error("Modify thread pool [{}] failed, pool not found", newConfig);
            throw new NoSuchNamedThreadPoolException("pool not found");
        }

        if (newConfig.getCorePoolSize() < 0 || newConfig.getMaxPoolSize() < 0 || newConfig.getCorePoolSize() > newConfig.getMaxPoolSize()) {
            log.error("Modify thread pool [{}] failed, coreSize or maxSize < 0 or coreSize > maxSize", poolName);
            throw new IllegalArgumentException("coreSize or maxSize < 0 or coreSize > maxSize");
        }

        if (newConfig.getKeepAliveTimeMs() != null && newConfig.getKeepAliveTimeMs() < 0) {
            log.error("Modify thread pool [{}] failed, keepAliveTimeMs < 0", poolName);
            throw new IllegalArgumentException("keepAliveTimeMs < 0");
        }

        if (newConfig.getMonitor() != null
                && (newConfig.getMonitor().getEnabled() && (newConfig.getMonitor().getTimeWindowSeconds() == null || newConfig.getMonitor().getTimeWindowSeconds() <= 0))) {
            log.error("Modify thread pool [{}] failed, monitor intervalMs <= 0", poolName);
            throw new IllegalArgumentException("monitor intervalMs <= 0");
        }

        RejectPolicyEnum.getPolicyByName(newConfig.getPolicy());
    }


    private boolean isThreadPoolNotExist(String poolName) {
        return !applicationContext.containsBean(poolName) || !(applicationContext.getBean(poolName) instanceof DynamicThreadPool);
    }


    public ThreadPoolConfig getPoolConfig(String poolName) {
        if (isThreadPoolNotExist(poolName)) {
            return null;
        }
        DynamicThreadPool threadPool = applicationContext.getBean(poolName, DynamicThreadPool.class);

        ThreadPoolConfig threadPoolConfig = threadPool.getThreadPoolConfig();

        String monitorUrl = threadPoolConfig.getMonitor().getMonitorUrl();

        DynamicThreadPoolProperties.GlobalMonitorConfig monitor = dynamicThreadPoolProperties.getMonitor();
        if (StringUtils.isBlank(monitorUrl) && monitor != null && StringUtils.isNotBlank(monitor.getBaseMonitorUrl())) {
            // &var-application=thread-monitor-demo&var-thread_pool=otherThreadPool&var-task_name=All
            monitorUrl = monitor.getBaseMonitorUrl() + "&var-application=" + applicationName + "var-server_ip=" + IpUtil.getServerIp() + "&var-thread_pool=" + poolName;
        }

        threadPoolConfig.getMonitor().setMonitorUrl(monitorUrl);

        return threadPoolConfig;
    }

    public List<ThreadPoolConfig> getAllPoolConfig() {
        String[] beanNamesForType = applicationContext.getBeanNamesForType(DynamicThreadPool.class);
        List<ThreadPoolConfig> collect = Stream.of(beanNamesForType).map(this::getPoolConfig)
                .filter(Objects::nonNull).sorted(Comparator.comparingInt(ThreadPoolConfig::getCorePoolSize).reversed())
                .toList();
        return new ArrayList<>(collect);
    }

    public void resetThreadPool(String poolName) {
        resetThreadPool(poolName, new ThreadPoolConfig());
    }

    public void resetThreadPool(@Nonnull String poolName, ThreadPoolConfig newConfig) {
        log.info("Reset thread pool: {}", poolName);

        ReentrantReadWriteLock lock = LOCK_MAP.computeIfAbsent(poolName, k -> new ReentrantReadWriteLock());
        lock.writeLock().lock();
        try {
            if (isThreadPoolNotExist(poolName)) {
                return;
            }
            DynamicThreadPool oldPool = applicationContext.getBean(poolName, DynamicThreadPool.class);

            // 创建新的线程池
            newConfig.setPoolName(poolName);
            DynamicThreadPool newPool = new DynamicThreadPool(newConfig);

            BlockingQueue<Runnable> oldPoolQueue = oldPool.getQueue();

            BlockingQueue<Runnable> tmpQueue;
            // 将旧线程池的任务转移到新线程池中 必须稍后再执行，否则 指标会在调用 shutdown 之后被清除
            if (!oldPoolQueue.isEmpty()) {
                tmpQueue = new LinkedBlockingQueue<>(oldPoolQueue.size());
                oldPoolQueue.drainTo(tmpQueue);
            } else {
                tmpQueue = null;
            }

            // 关闭旧的线程池
            oldPool.shutdown();

            // 获取BeanDefinitionRegistry
            ConfigurableApplicationContext configurableApplicationContext = (ConfigurableApplicationContext) applicationContext;
            BeanDefinitionRegistry beanDefinitionRegistry = (BeanDefinitionRegistry) configurableApplicationContext.getBeanFactory();

            // 创建新的BeanDefinition
            BeanDefinition beanDefinition = BeanDefinitionBuilder.genericBeanDefinition(DynamicThreadPool.class, () -> newPool).getBeanDefinition();

            // 重新注册Bean
            beanDefinitionRegistry.removeBeanDefinition(poolName);
            beanDefinitionRegistry.registerBeanDefinition(poolName, beanDefinition);

            // 在新线程中执行任务转移，尽快释放锁
            if (tmpQueue != null) {
                new Thread(() -> {
                    while (!tmpQueue.isEmpty()) {
                        try {
                            Runnable task = tmpQueue.poll();
                            if (task != null) {
                                newPool.execute(task);
                            }
                        } catch (Exception e) {
                            log.error("Transfer tasks from old thread pool [{}] to new thread pool [{}] failed", poolName, newConfig.getPoolName(), e);
                        }
                    }
                    log.info("Transfer tasks from old thread pool [{}] to new thread pool [{}] success", poolName, newConfig.getPoolName());

                }).start();
            }

            log.info("Reset thread pool [{}] success", poolName);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * DynamicThreadPool 只能通过该方法 或者 ApplicationContext.getBean(poolName, DynamicThreadPool.class) 获取
     *
     * @param poolName 线程池名称
     * @return DynamicThreadPool
     */
    public Optional<DynamicThreadPool> getDynamicThreadPool(@Nonnull String poolName) {
        ReentrantReadWriteLock lock = LOCK_MAP.computeIfAbsent(poolName, k -> new ReentrantReadWriteLock());
        lock.readLock().lock();
        try {
            if (isThreadPoolNotExist(poolName)) {
                return Optional.empty();
            }
            return Optional.of(applicationContext.getBean(poolName, DynamicThreadPool.class));
        } finally {
            lock.readLock().unlock();
        }
    }

    public void switchMonitor(SwitchMonitorParam param) {
        String poolName = param.poolName();
        if (isThreadPoolNotExist(poolName)) {
            return;
        }
        boolean enable = param.enableMonitor();
        DynamicThreadPool dynamicThreadPool = applicationContext.getBean(poolName, DynamicThreadPool.class);
        final MonitorConfig oldMonitor = dynamicThreadPool.getThreadPoolConfig().getMonitor();
        long ms = oldMonitor.getTimeWindowSeconds() == null ? DefaultConfigConstants.DEFAULT_TIME_WINDOW_SECONDS : oldMonitor.getTimeWindowSeconds();
        dynamicThreadPool.updateMonitor(new MonitorConfig(enable,
                enable ? ms : null,
                oldMonitor.getMonitorUrl()));
    }
}
