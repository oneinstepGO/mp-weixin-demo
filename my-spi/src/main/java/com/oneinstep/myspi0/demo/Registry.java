package com.oneinstep.myspi0.demo;

import com.oneinstep.myspi0.core.URL;

/**
 * 注册中心接口
 * 这里只是为了演示 SPI 的使用，所以省略其它方法
 */
public interface Registry {
    /**
     * 注册服务
     */
    void register(URL url);
}
