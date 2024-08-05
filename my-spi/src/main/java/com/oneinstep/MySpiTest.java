package com.oneinstep;

import com.oneinstep.myspi.core.ExtensionLoader;
import com.oneinstep.myspi.core.URL;
import com.oneinstep.myspi.demo.MyExtensionPostProcessor;
import com.oneinstep.myspi.demo.Registry;
import com.oneinstep.myspi.demo.RegistryFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * 演示 dubbo SPI 的使用
 */
@Slf4j
public class MySpiTest {

    public static void main(String[] args) {
        // 获取扩展点加载器
        ExtensionLoader<RegistryFactory> extensionLoader = ExtensionLoader.getExtensionLoader(RegistryFactory.class);
        // 添加扩展点处理器
        extensionLoader.addExtensionPostProcessor(new MyExtensionPostProcessor());
        printCurrExtensions(extensionLoader);

        // 获取自适应扩展点
        RegistryFactory registryFactory = extensionLoader.getAdaptiveExtension();
        printCurrExtensions(extensionLoader);


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
        printCurrExtensions(extensionLoader);


        URL nacosUrl = new URL("nacos", "127.0.0.1", 8848, "com.demo.RegistryService", urlParams);
        // 获取 nacos 注册中心
        Registry nacosRegistry = registryFactory.getRegistry(nacosUrl);
        // 将服务注册到 nacos 注册中心
        nacosRegistry.register(serviceUrl);
        printCurrExtensions(extensionLoader);

        log.info("=====================================");

        // 获取指定 key 的扩展点
        RegistryFactory zookeeperRegistryFactory = extensionLoader.getExtension("zookeeper");
        // 获取 zookeeper 注册中心
        Registry zookeeperRegistry = zookeeperRegistryFactory.getRegistry(zkUrl);
        // 将服务注册到 zookeeper 注册中心
        zookeeperRegistry.register(serviceUrl);
        printCurrExtensions(extensionLoader);


        ExtensionLoader.destroy();
        printCurrExtensions(extensionLoader);

    }

    private static void printCurrExtensions(ExtensionLoader<RegistryFactory> extensionLoader) {
        log.info("printCurrExtensions => AllExtensionLoaderTypes = {}, CachedClassesNames = {}, ExtensionInstances = {}",
                ExtensionLoader.getAllExtensionLoaderTypes(),
                extensionLoader.getCachedClassesName(),
                extensionLoader.getExtensionInstances());
    }

}
