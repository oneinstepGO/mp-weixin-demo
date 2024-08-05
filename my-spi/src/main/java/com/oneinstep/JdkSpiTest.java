package com.oneinstep;

import com.oneinstep.jdkspi.MyService;

import java.util.ServiceLoader;

/**
 * 演示 JDK SPI 的使用
 */
public class JdkSpiTest {

    public static void main(String[] args) {
        // 通过 ServiceLoader 加载所有的 MyService 接口实现类
        ServiceLoader<MyService> serviceLoader = ServiceLoader.load(MyService.class);
        for (MyService service : serviceLoader) {
            service.execute();
        }
    }
}
