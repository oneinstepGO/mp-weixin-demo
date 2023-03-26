package com.oneinstep.demo.spring.routing;

import java.lang.annotation.*;

/**
 * 标记 路由 Bean
 * @author aaron.shaw
 * @since 2023-03-26 20:36
 **/

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RoutingBean {

}
