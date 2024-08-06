package com.oneinstep.myrpc.core.client;

import com.oneinstep.myrpc.core.annotation.RpcReference;
import jakarta.annotation.Resource;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

/**
 * RPC proxy
 * Scan the fields of the bean, and if the field is annotated with @RpcReference, create a proxy for the field
 */
@Component
public class RpcClientProxy implements BeanPostProcessor {

    /**
     * Service registry factory
     */
    @Resource
    private RpcServiceProxyFactory rpcServiceProxyFactory;


    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        // Scan the fields of the bean, and if the field is annotated with @RpcReference, create a proxy for the field
        Field[] fields = bean.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(RpcReference.class)) {
                RpcReference annotation = field.getAnnotation(RpcReference.class);
                String version = annotation.version();
                if (version == null || version.isEmpty()) {
                    version = "DEFAULT";
                }
                // Create a proxy for the field
                Object proxy = rpcServiceProxyFactory.createProxy(field.getType(), version);
                // Set the field to be accessible
                field.setAccessible(true);
                // Set the proxy object to the field
                ReflectionUtils.setField(field, bean, proxy);
            }
        }
        return bean;
    }
}
