package com.oneinstep.spi.demo;

import com.oneinstep.spi.core.Adaptive;
import com.oneinstep.spi.core.SPI;
import com.oneinstep.spi.core.URL;

/**
 * 注册中心工厂接口
 */
@SPI
public interface RegistryFactory {

    /**
     * 连接注册中心
     *
     * @param url 注册中心地址
     * @return 注册中心对象
     */
    @Adaptive("protocol")
    Registry getRegistry(URL url);
}