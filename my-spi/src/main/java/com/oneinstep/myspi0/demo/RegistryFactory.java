package com.oneinstep.myspi0.demo;

import com.oneinstep.myspi0.core.Adaptive;
import com.oneinstep.myspi0.core.SPI;
import com.oneinstep.myspi0.core.URL;

/**
 * 注册中心工厂接口
 */
@SPI
public interface RegistryFactory {

    /**
     * 获取注册中心
     *
     * @param url 注册中心地址
     * @return 注册中心对象
     */
    @Adaptive("protocol")
    Registry getRegistry(URL url);
}