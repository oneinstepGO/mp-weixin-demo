package com.oneinstep.myrpc.demo.controller;

import com.oneinstep.myrpc.core.annotation.RpcReference;
import com.oneinstep.myrpc.demo.api.ExampleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Demo controller
 */
@RestController
@Slf4j
public class DemoController {

    private static final AtomicInteger COUNTER = new AtomicInteger(0);
    /**
     * Remote service reference
     */
    @RpcReference
    private ExampleService exampleService;

    @GetMapping("/sayHello")
    public String sayHello() {
        log.info("Calling remote service...");
        String result = exampleService.sayHello("World!", COUNTER.getAndIncrement());
        log.info("Call remote service >>> Result: {}", result);
        return result;
    }

}
