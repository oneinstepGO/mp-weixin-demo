package com.oneinstep.demo.controller;

import com.oneinstep.demo.api.DemoDubboService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@Slf4j
public class DemoController {

    @DubboReference
    private DemoDubboService demoDubboService;

    // for OOM test
    private static final List<String> OOM_LIST = new ArrayList<>();

    private static final String CONTENT = """
            The Benefits of Reading Books
            In an age dominated by digital screens and fleeting social media updates, \
            the timeless act of reading books remains a steadfast source of knowledge, \
            pleasure, and mental stimulation. \
            While e-books and audiobooks have modernized the way we consume literature, \
            the essence of reading — \
            whether from a physical book or a digital device \
            — continues to offer myriad benefits that extend beyond mere entertainment.""";

    @GetMapping("/hello")
    public String hello() {
        log.info("get request to /hello");
        String dubbo = demoDubboService.sayHello("stranger");
        for (int i = 0; i < 100; i++) {
            OOM_LIST.add(CONTENT);
        }
        log.info("OOM_LIST size: {}", OOM_LIST.size());
        return "get result from dubbo: " + dubbo;
    }

}
