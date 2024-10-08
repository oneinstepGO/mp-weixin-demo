package com.oneinstep.demo.service;

import com.oneinstep.demo.api.DemoDubboService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.Random;

@Slf4j
@DubboService
public class DemoDubboServiceImpl implements DemoDubboService {

    private static final Random RANDOM = new Random();

    @Override
    public String sayHello(String name) {
        int i = RANDOM.nextInt(100);
        if (i % 10 == 0) {
            throw new RuntimeException("Random Exception");
        }
        return "Hello From Dubbo, " + name;
    }

}
