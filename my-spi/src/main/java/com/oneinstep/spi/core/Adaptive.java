package com.oneinstep.spi.core;

import java.lang.annotation.*;

/**
 * 用于标记自适应扩展点的注解。
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Adaptive {

    /**
     * 决定注入哪个目标扩展。
     * 目标扩展名由 URL 中传递的参数决定，参数名由本方法给出。
     * 如果在 URL 中找不到指定的参数、则将使用默认扩展进行依赖注入（在其接口的 SPI 中指定）。
     * 例如，在 URL 中找到参数 "key1"，使用其值作为扩展名，
     * 如果在 URL 中找不到 "key1"（或其值为空），则使用默认扩展名 "key2"，
     * 如果 "key2 "也不存在，则抛出 IllegalStateException。
     * 如果参数名为空，则根据接口的类名生成默认参数名，
     * 规则是：将类名从大写字符分成几个部分，并用点". "分隔，
     * 例如，对于 org.apache.dubbo.xxx.YyyInvokerWrapper 而言，生成的名称是 "yyy.invoker.wrapper"。
     *
     * @return 在 URL 中的参数名
     */
    String[] value() default {};
}