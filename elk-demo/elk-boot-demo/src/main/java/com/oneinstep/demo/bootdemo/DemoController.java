package com.oneinstep.demo.bootdemo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class DemoController {

    @GetMapping("/hello")
    public String hello() {
        log.info("Hello World!");
        return "Hello World!";
    }

}
