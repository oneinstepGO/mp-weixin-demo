package com.oneinstep.spi.core.utils;

/**
 * 并发安全的 Holder
 *
 * @param <T> 泛型
 */
public class Holder<T> {

    private volatile T value;

    public void set(T value) {
        this.value = value;
    }

    public T get() {
        return value;
    }

}
