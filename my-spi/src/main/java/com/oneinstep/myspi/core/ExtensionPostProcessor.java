package com.oneinstep.myspi.core;

/**
 * 扩展点后置处理器接口
 * 在 instance 实例化之前和之后进行处理
 */
public interface ExtensionPostProcessor {

    default Object postProcessBeforeInitialization(Object instance, String name) {
        return instance;
    }

    default Object postProcessAfterInitialization(Object instance, String name) {
        return instance;
    }

}
