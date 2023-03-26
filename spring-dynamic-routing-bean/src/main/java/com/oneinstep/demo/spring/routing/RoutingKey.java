package com.oneinstep.demo.spring.routing;

import java.lang.annotation.*;

/**
 * 标记 路由 key
 *
 * @author aaron.shaw
 * @since 2023-03-26 20:39
 **/
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RoutingKey {

}
