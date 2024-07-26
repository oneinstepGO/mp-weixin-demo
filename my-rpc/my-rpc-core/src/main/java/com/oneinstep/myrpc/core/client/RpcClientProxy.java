package com.oneinstep.myrpc.core.client;

import com.oneinstep.myrpc.core.annotation.RpcReference;
import com.oneinstep.myrpc.core.registry.ServiceRegistry;
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
     * Service registry
     */
    private final ServiceRegistry serviceRegistry;

    public RpcClientProxy(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

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
                // Create a proxy for the field
                Object proxy = new RpcServiceProxy(serviceRegistry).createProxy(field.getType());
                // Set the field to be accessible
                field.setAccessible(true);
                // Set the proxy object to the field
                ReflectionUtils.setField(field, bean, proxy);
            }
        }
        return bean;
    }
}
