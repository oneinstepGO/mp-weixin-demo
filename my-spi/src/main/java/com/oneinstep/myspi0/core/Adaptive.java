package com.oneinstep.myspi0.core;

import java.lang.annotation.*;

/**
 * 用于标记自适应扩展点的注解。
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Adaptive {

    /**
     * 决定注入哪个目标扩展
     *
     * @return 根据哪个参数获取扩展点
     */
    String value() default "";
}