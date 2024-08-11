package com.oneinstep.myspi0;

import com.oneinstep.myspi0.core.ExtensionLoader;
import com.oneinstep.myspi0.core.URL;
import com.oneinstep.myspi0.demo.Registry;
import com.oneinstep.myspi0.demo.RegistryFactory;

public class MySpi0Test {

    public static void main(String[] args) {
        // 获取扩展点加载器
        ExtensionLoader<RegistryFactory> extensionLoader = ExtensionLoader.getExtensionLoader(RegistryFactory.class);
        // 获取自适应扩展点
        RegistryFactory registryFactory = extensionLoader.getAdaptiveExtension();
        System.out.println(extensionLoader.getExtensionInstances());
        // 服务提供者 URL
        URL serviceUrl = new URL("dubbo", "127.0.0.1", 20880, "com.demo.DemoService", null);
        // 根据 URL 参数获取注册中心
        URL zkUrl = new URL("zookeeper", "127.0.0.1", 2181, "", null);
        // 获取 zookeeper 注册中心
        Registry zkRegistry = registryFactory.getRegistry(zkUrl);
        // 将服务注册到 zookeeper 注册中心
        zkRegistry.register(serviceUrl);
        System.out.println(extensionLoader.getExtensionInstances());

        URL nacosUrl = new URL("nacos", "127.0.0.1", 8848, "", null);
        // 获取 nacos 注册中心
        Registry nacosRegistry = registryFactory.getRegistry(nacosUrl);
        // 将服务注册到 nacos 注册中心
        nacosRegistry.register(serviceUrl);
        System.out.println(extensionLoader.getExtensionInstances());
        // 获取指定 key 的扩展点
        RegistryFactory zookeeperRegistryFactory = extensionLoader.getExtension("zookeeper");
        // 获取 zookeeper 注册中心
        Registry zookeeperRegistry = zookeeperRegistryFactory.getRegistry(zkUrl);
        // 将服务注册到 zookeeper 注册中心
        zookeeperRegistry.register(serviceUrl);
        System.out.println(extensionLoader.getExtensionInstances());
    }
}
