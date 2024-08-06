package com.oneinstep.myrpc.demo.controller;

import com.oneinstep.myrpc.core.annotation.RpcReference;
import com.oneinstep.myrpc.core.exception.RpcException;
import com.oneinstep.myrpc.core.exception.ServiceNotFoundException;
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
    @RpcReference(version = "1.0")
//    @RpcReference(version = "2.0")
    private ExampleService exampleService;

    @GetMapping("/sayHello")
    public String sayHello() {
        log.info("Calling remote service...");
        String result;
        try {
            result = exampleService.sayHello("World!", COUNTER.getAndIncrement());
        } catch (ServiceNotFoundException serviceNotFoundException) {
            return serviceNotFoundException.getMessage();
        } catch (RpcException rpcException) {
            return "Failed to call remote service: " + rpcException.getMessage();
        }
        log.info("Call remote service >>> Result: {}", result);
        return result;
    }

}
