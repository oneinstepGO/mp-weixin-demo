package com.oneinstep.myrpc.core.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * mark the class as a remote service
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface RpcService {
    /**
     * service interface
     *
     * @return service interface
     */
    Class<?> value();

    /**
     * service version
     *
     * @return service version
     */
    String version() default "DEFAULT";
}