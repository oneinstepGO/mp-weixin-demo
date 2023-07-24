package com.oneinstep.apt;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义的 Getter 注解
 *
 * @author aaron.shaw
 * @date 2023-07-21 01:03
 **/
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.SOURCE)
public @interface MyGetter {
}
