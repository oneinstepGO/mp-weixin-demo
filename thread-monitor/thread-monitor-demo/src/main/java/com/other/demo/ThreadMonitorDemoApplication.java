package com.other.demo;

import com.oneinstep.jupiter.threadpool.DynamicThreadPoolManager;
import com.oneinstep.jupiter.threadpool.EnableDynamicThreadPool;
import com.oneinstep.jupiter.threadpool.NamedRunnable;
import com.oneinstep.jupiter.threadpool.support.NoSuchNamedThreadPoolException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.util.Random;


@SpringBootApplication
@EnableDynamicThreadPool
@Slf4j
public class ThreadMonitorDemoApplication implements CommandLineRunner {

    @Resource
    private DynamicThreadPoolManager dynamicThreadPoolManager;
    @Resource
    private ApplicationContext applicationContext;

    public static void main(String[] args) {
        SpringApplication.run(ThreadMonitorDemoApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        String property = applicationContext.getEnvironment().getProperty("dynamic-thread-pool.monitor.export.step");
        log.info("dynamic-thread-pool.monitor.export.step: {}", property);


        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Random random = new Random();
        new Thread(() -> {

            while (true) {
                try {

                    dynamicThreadPoolManager.getDynamicThreadPool("bizThreadPool").orElseThrow(() -> new NoSuchNamedThreadPoolException("bizThreadPool"))
                            .execute(new NamedRunnable("QueryUserInfoTask", () -> {

                                // 模拟任务执行时间
                                try {
                                    Thread.sleep(100L + random.nextInt(50));
                                } catch (InterruptedException e) {
                                }

                                int i = random.nextInt(100);
                                if (i % 20 == 0) {
//                            throw new RuntimeException("QueryUserInfoTask failed");
                                }

                            }));
                    try {
                        Thread.sleep(20L + random.nextInt(10));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
//                    e.printStackTrace();
                }
            }

        }).start();

        new Thread(() -> {

            while (true) {
                try {
                    dynamicThreadPoolManager.getDynamicThreadPool("bizThreadPool").orElseThrow(() -> new NoSuchNamedThreadPoolException("bizThreadPool"))
                            .execute(new NamedRunnable("QueryProductListTask", () -> {

                                // 模拟任务执行时间
                                try {
                                    Thread.sleep(500L + random.nextInt(100));
                                } catch (InterruptedException e) {
                                }
                                int i = random.nextInt(100);
                                if (i % 20 == 0) {
                                    throw new RuntimeException("QueryProductListTask failed");
                                }

                            }));
                    try {
                        Thread.sleep(50L + random.nextInt(25));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
//                    e.printStackTrace();
                }

            }
        }).start();

        new Thread(() -> {

            while (true) {
                try {
                    dynamicThreadPoolManager.getDynamicThreadPool("bizThreadPool").orElseThrow(() -> new NoSuchNamedThreadPoolException("bizThreadPool"))
                            .execute(new NamedRunnable("QueryPromotionTask", () -> {

                                // 模拟任务执行时间
                                try {
                                    Thread.sleep(200L + random.nextInt(80));
                                } catch (InterruptedException e) {
                                }
                                int i = random.nextInt(100);
                                if (i % 15 == 0) {
//                            throw new RuntimeException("QueryPromotionTask failed");
                                }

                            }));
                    try {
                        Thread.sleep(20L + random.nextInt(10));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
//                    e.printStackTrace();
                }

            }
        }).start();


        new Thread(() -> {

            while (true) {
                try {
                    dynamicThreadPoolManager.getDynamicThreadPool("otherThreadPool").orElseThrow(() -> new NoSuchNamedThreadPoolException("otherThreadPool"))
                            .execute(new NamedRunnable("clearCacheTask", () -> {

                                // 模拟任务执行时间
                                try {
                                    Thread.sleep(800L + random.nextInt(200));
                                } catch (InterruptedException e) {
                                }
                                int i = random.nextInt(100);
                                if (i % 15 == 0) {
//                            throw new RuntimeException("QueryPromotionTask failed");
                                }

                            }));
                    try {
                        Thread.sleep(200L + random.nextInt(50));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
//                    e.printStackTrace();
                }

            }
        }).start();

    }
}
