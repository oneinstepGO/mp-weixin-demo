package com.oneinstep.myrpc.demo.impl;

import com.oneinstep.myrpc.core.annotation.RpcService;
import com.oneinstep.myrpc.demo.api.ExampleService;
import lombok.extern.slf4j.Slf4j;

/**
 * Example service implementation
 */
@RpcService(value = ExampleService.class, version = "1.0")
@Slf4j
public class ExampleServiceImpl implements ExampleService {

    @Override
    public String sayHello(String name, int times) {
        log.info("ExampleServiceImpl >>>> Hello, {} ({})", name, times);
//        throw new RuntimeException("Exception test");
        return "Hello, " + name + " (" + times + ")";
    }

}
