package com.oneinstep.spi.demo;

import com.oneinstep.spi.core.ExtensionPostProcessor;
import lombok.extern.slf4j.Slf4j;

/**
 * 自定义 ExtensionPostProcessor
 */
@Slf4j
public class MyExtensionPostProcessor implements ExtensionPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object instance, String name) {
        log.info("before ExtensionPostProcessor: {} ...", instance);
        return instance;
    }

    @Override
    public Object postProcessAfterInitialization(Object instance, String name) {
        log.info("after ExtensionPostProcessor: {} ...", instance);
        return instance;
    }

}
