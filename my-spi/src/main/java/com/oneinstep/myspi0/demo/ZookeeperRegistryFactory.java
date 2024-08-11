package com.oneinstep.myspi0.demo;

import com.oneinstep.myspi0.core.URL;
import lombok.extern.slf4j.Slf4j;

/**
 * Zookeeper 注册中心工厂
 */
@Slf4j
public class ZookeeperRegistryFactory extends AbstractRegistryFactory {

    @Override
    public Registry createRegistry(URL url) {
        log.info("创建 ZooKeeper 注册中心, URL: {}", url.getPathAddress());
        return serviceURL -> log.info("将服务注册到 ZooKeeper, 服务URL: {}", serviceURL.getPathAddress());
    }

}
