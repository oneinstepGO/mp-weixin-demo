package com.oneinstep.myspi.core;

import java.lang.annotation.*;

/**
 * 标记扩展点接口
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface SPI {

    /**
     * default extension name
     */
    String value() default "";
}