package com.oneinstep.myspi.extend;

import com.oneinstep.myspi.core.URL;
import lombok.extern.slf4j.Slf4j;

/**
 * Nacos 注册中心工厂
 */
@Slf4j
public class NacosRegistryFactory extends AbstractRegistryFactory {

    @Override
    public Registry createRegistry(URL url) {
        log.info("创建 Nacos 注册中心, URL: {}", url.getPathAddress());
        return serviceURL -> log.info("将服务注册到 Nacos, 服务URL: {}", serviceURL.getPathAddress());
    }

}
