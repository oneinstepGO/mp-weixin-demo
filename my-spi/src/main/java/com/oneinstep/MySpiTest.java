package com.oneinstep;

import com.oneinstep.spi.core.ExtensionLoader;
import com.oneinstep.spi.core.URL;
import com.oneinstep.spi.demo.MyExtensionPostProcessor;
import com.oneinstep.spi.demo.Registry;
import com.oneinstep.spi.demo.RegistryFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * dubbo spi 测试类
 */
public class MySpiTest {

    public static void main(String[] args) {
        // 获取扩展点加载器
        ExtensionLoader<RegistryFactory> extensionLoader = ExtensionLoader.getExtensionLoader(RegistryFactory.class);

        // 添加扩展点处理器
        extensionLoader.addExtensionPostProcessor(new MyExtensionPostProcessor());

        // 获取自适应扩展点
        RegistryFactory registryFactory = extensionLoader.getAdaptiveExtension();

        Map<String, String> urlParams = new HashMap<>();
        urlParams.put("application", "my-spi");

        // dubbo://127.0.0.1:20880/com.demo.DemoService?application=my-spi
        // 服务提供者 URL
        URL serviceUrl = new URL("dubbo", "127.0.0.1", 20880, "com.demo.DemoService", urlParams);

        // 根据 URL 参数获取注册中心
        // zookeeper://127.0.0.1:2181/com.demo.RegistryService?application=my-spi
        URL zkUrl = new URL("zookeeper", "127.0.0.1", 2181, "com.demo.RegistryService", urlParams);
        // 获取 zookeeper 注册中心
        Registry zkRegistry = registryFactory.getRegistry(zkUrl);
        // 将服务注册到 zookeeper 注册中心
        zkRegistry.register(serviceUrl);

        URL nacosUrl = new URL("nacos", "127.0.0.1", 8848, "com.demo.RegistryService", urlParams);
        // 获取 nacos 注册中心
        Registry nacosRegistry = registryFactory.getRegistry(nacosUrl);
        // 将服务注册到 nacos 注册中心
        nacosRegistry.register(serviceUrl);


        System.out.println("=====================================");

        // 获取指定 key 的扩展点
        RegistryFactory zookeeperRegistryFactory = extensionLoader.getExtension("zookeeper");
        // 获取 zookeeper 注册中心
        Registry zookeeperRegistry = zookeeperRegistryFactory.getRegistry(zkUrl);
        // 将服务注册到 zookeeper 注册中心
        zookeeperRegistry.register(serviceUrl);

        ExtensionLoader.destroy();
    }
}
