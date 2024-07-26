package com.oneinstep.myrpc.core.annotation;

import java.lang.annotation.*;

/**
 * mark the field as a reference to the remote service
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RpcReference {}