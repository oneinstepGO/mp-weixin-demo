package com.oneinstep.jupiter.threadpool;

import com.oneinstep.jupiter.threadpool.metrics.ThreadPoolMetricsCollector;
import com.oneinstep.jupiter.threadpool.support.RejectPolicyEnum;
import io.micrometer.core.instrument.util.NamedThreadFactory;
import jakarta.annotation.Nonnull;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * A named thread pool.
 * 可以实时监控线程池的任务执行情况
 * {{@link ThreadPoolMetricsCollector}}
 * 需要使用 namedThreadPool.execute(new NamedTask("taskName", runnable)) 来提交任务，而非 submit
 * 唯一获取DynamicThreadPool线程池实例的方式是通过 {{@link DynamicThreadPoolManager#getDynamicThreadPool(String)}}
 * <p>
 * or <code>ApplicationContext.getBean(poolName, DynamicThreadPool.class)</code>
 */
@Slf4j
public class DynamicThreadPool extends ThreadPoolExecutor {

    private final ThreadPoolConfig threadPoolConfig;

    // 线程池名称
    @Getter
    private final String poolName;

    // 线程池监控收集器
    private volatile ThreadPoolMetricsCollector collector;

    public DynamicThreadPool(final ThreadPoolConfig threadPoolConfig) {
        super(threadPoolConfig.getCorePoolSize(), threadPoolConfig.getMaxPoolSize(),
                threadPoolConfig.getKeepAliveTimeMs(), TimeUnit.MILLISECONDS,
                threadPoolConfig.getWorkQueue().createQueue(), new NamedThreadFactory(threadPoolConfig.getPoolName()),
                new RejectionHandlerWrapperWithCounting(RejectPolicyEnum.createRejectedExecutionHandler(threadPoolConfig.getPolicy())));
        this.threadPoolConfig = threadPoolConfig;
        this.poolName = threadPoolConfig.getPoolName();
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        super.beforeExecute(t, r);
        if (r instanceof NamedRunnable namedRunnable) {
            namedRunnable.setStartTime(System.currentTimeMillis());
        } else {
            log.warn("NamedRunnable is required, but got {}", r.getClass());
            throw new IllegalArgumentException("NamedRunnable is required");
        }
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        if (r instanceof NamedRunnable task && this.threadPoolConfig.getMonitor().getEnabled()) {
            String taskName = task.getName();
            long executionTime = System.currentTimeMillis() - task.getStartTime();
            getCollector().ifPresent(metricsCollector -> {
                boolean taskRegistered = metricsCollector.isTaskRegistered(taskName);
                if (!taskRegistered) {
                    synchronized (this) {
                        if (!metricsCollector.isTaskRegistered(taskName)) {
                            metricsCollector.registerTaskMetrics(taskName);
                        }
                    }
                }
                recordTask(metricsCollector, t, taskName, executionTime);
            });
        }
    }

    // 获取监控收集器
    private Optional<ThreadPoolMetricsCollector> getCollector() {
        if (Boolean.FALSE.equals(this.threadPoolConfig.getMonitor().getEnabled())) {
            return Optional.empty();
        }
        if (this.collector == null) {
            synchronized (this) {
                if (this.collector == null && this.threadPoolConfig.getMonitor().getEnabled()) {
                    log.info("Creating ThreadPoolMetricsCollector for {}", this.threadPoolConfig.getPoolName());
                    this.collector = new ThreadPoolMetricsCollector(this);
                }
            }
        }
        return Optional.of(this.collector);
    }

    // 记录任务执行情况
    private void recordTask(ThreadPoolMetricsCollector metricsCollector, Throwable t, String name, long executionTime) {
        metricsCollector.increaseTaskTotalCount(name);
        metricsCollector.addTaskExecutionTime(name, executionTime);
        if (t == null) {
            metricsCollector.increaseTaskSuccessCount(name);
        } else {
            metricsCollector.increaseTaskFailureCount(name);
        }
    }

    // 更新监控配置
    public synchronized void updateMonitor(final MonitorConfig newMonitorConfig) {
        final boolean enabled = newMonitorConfig.getEnabled();
        final Long newTimeWindowSeconds = enabled ? newMonitorConfig.getTimeWindowSeconds() : null;
        final MonitorConfig oldMonitorConfig = this.threadPoolConfig.getMonitor();
        final String newMonitorUrl = newMonitorConfig.getMonitorUrl() == null ? oldMonitorConfig.getMonitorUrl() : newMonitorConfig.getMonitorUrl();
        this.threadPoolConfig.getMonitor().setMonitorUrl(newMonitorUrl);
        if (enabled) {
            if (Boolean.FALSE.equals(oldMonitorConfig.getEnabled())) {
                // 旧配置未启用，直接启用新配置
                this.threadPoolConfig.getMonitor().setEnabled(true);
                this.threadPoolConfig.getMonitor().setTimeWindowSeconds(newTimeWindowSeconds);
                this.collector = new ThreadPoolMetricsCollector(this);
            } else {
                // 旧配置已启用，检查时间窗口是否变化
                if (!Objects.equals(oldMonitorConfig.getTimeWindowSeconds(), newTimeWindowSeconds)) {
                    // 更新监控配置
                    this.threadPoolConfig.getMonitor().setEnabled(true);
                    this.threadPoolConfig.getMonitor().setTimeWindowSeconds(newTimeWindowSeconds);

                    ThreadPoolMetricsCollector oldCollector = this.collector;

                    try {
                        // 创建新的收集器
                        this.collector = new ThreadPoolMetricsCollector(this);
                        // 停止旧的收集器
                        oldCollector.stop();

                    } catch (Exception e) {
                        log.error("Failed to create new ThreadPoolMetricsCollector", e);
                        // 恢复旧的收集器和监控配置
                        this.collector = oldCollector;
                        this.threadPoolConfig.getMonitor().setEnabled(oldMonitorConfig.getEnabled());
                        this.threadPoolConfig.getMonitor().setTimeWindowSeconds(oldMonitorConfig.getTimeWindowSeconds());
                    }
                } else {
                    log.warn("Monitor intervalMs is not changed, ignore update");
                }
            }
        } else {
            disableMonitor(oldMonitorConfig);
        }
    }

    private void disableMonitor(final MonitorConfig oldMonitorConfig) {
        if (Boolean.TRUE.equals(oldMonitorConfig.getEnabled())) {
            synchronized (this) {
                this.threadPoolConfig.getMonitor().setEnabled(false);
                this.threadPoolConfig.getMonitor().setTimeWindowSeconds(null);

                log.info("Disabling monitor for {}, monitorConfig: {}", this.poolName, threadPoolConfig.getMonitor());
                if (this.collector != null) {
                    this.collector.stop();
                    this.collector = null;
                }
            }
        } else {
            log.warn("Monitor is not enabled, ignore update");
        }
    }


    // 拒绝策略
    @Getter
    public static class RejectionHandlerWrapperWithCounting implements RejectedExecutionHandler {

        private final RejectedExecutionHandler realHandler;

        public RejectionHandlerWrapperWithCounting(RejectedExecutionHandler realHandler) {
            this.realHandler = realHandler;
        }

        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {

            if (executor instanceof DynamicThreadPool dynamicThreadPool && r instanceof NamedRunnable task && dynamicThreadPool.threadPoolConfig.getMonitor().getEnabled()) {
                dynamicThreadPool.getCollector().ifPresent(metricsCollector -> metricsCollector.increaseTaskRejectedCount(task.getName()));
//                log.warn("Task {} is rejected", task.getName());
            }

            realHandler.rejectedExecution(r, executor);
        }

    }

    public int getQueueSize() {
        return getQueue().size();
    }

    public int getRemainingQueueCapacity() {
        return getQueue().remainingCapacity();
    }

    @Override
    public void shutdown() {
        super.shutdown();
        try {
            try {
                if (!this.awaitTermination(30, TimeUnit.SECONDS)) {
                    this.shutdownNow();
                }
            } catch (InterruptedException e) {
                this.shutdownNow();
                Thread.currentThread().interrupt();
            }
        } finally {
            if (this.collector != null) {
                this.collector.stop();
            }
            this.collector = null;
        }


    }

    @Override
    public List<Runnable> shutdownNow() {
        List<Runnable> tasks;
        try {
            tasks = super.shutdownNow();
        } finally {
            if (this.collector != null) {
                this.collector.stop();
            }
            this.collector = null;
        }

        return tasks;
    }

    @Override
    public synchronized void setMaximumPoolSize(int maximumPoolSize) {
        super.setMaximumPoolSize(maximumPoolSize);
        this.threadPoolConfig.setMaxPoolSize(maximumPoolSize);
    }

    @Override
    public synchronized void setCorePoolSize(int corePoolSize) {
        super.setCorePoolSize(corePoolSize);
        this.threadPoolConfig.setCorePoolSize(corePoolSize);
    }

    @Override
    public synchronized void setKeepAliveTime(long time, TimeUnit unit) {
        super.setKeepAliveTime(time, unit);
        this.threadPoolConfig.setKeepAliveTimeMs(unit.toMillis(time));
    }

    @Override
    public synchronized void setRejectedExecutionHandler(@Nonnull RejectedExecutionHandler newHandler) {
        super.setRejectedExecutionHandler(new RejectionHandlerWrapperWithCounting(newHandler));
        this.threadPoolConfig.setPolicy(RejectPolicyEnum.getPolicyByClass(newHandler.getClass()).policyName());
    }

    public ThreadPoolConfig getThreadPoolConfig() {
        // 为了防止外部修改配置，返回一个新的对象
        return this.threadPoolConfig.copy();
    }

}
