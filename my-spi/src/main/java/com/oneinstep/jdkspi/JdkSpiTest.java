package com.oneinstep.jdkspi;

import lombok.extern.slf4j.Slf4j;

import java.util.ServiceLoader;

/**
 * 演示 JDK SPI 的使用
 */
@Slf4j
public class JdkSpiTest {

    public static void main(String[] args) {
        // 通过 ServiceLoader 加载所有的 MyService 接口实现类
        ServiceLoader<MyService> serviceLoader = ServiceLoader.load(MyService.class);

        log.info("总共加载到 {} 个 MyService 接口实现类", serviceLoader.stream().count());

        for (MyService service : serviceLoader) {
            service.execute();
        }
    }
}
