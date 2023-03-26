package com.oneinstep.demo.spring.routing;

import java.lang.annotation.*;

/**
 * 标记路由规则
 *
 * @author aaron.shaw
 * @since 2023-03-26 21:24
 **/
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RoutingRule {

    String[] values();

}
