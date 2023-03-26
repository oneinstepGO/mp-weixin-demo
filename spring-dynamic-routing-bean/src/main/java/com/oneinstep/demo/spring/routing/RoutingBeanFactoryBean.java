package com.oneinstep.demo.spring.routing;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 创建的Bean代理的工厂Bean
 *
 * @author aaron.shaw
 * @since 2023-03-26 21:24
 **/
public class RoutingBeanFactoryBean<T> implements FactoryBean<T>, ApplicationContextAware {

    private ApplicationContext applicationContext;

    /**
     * 代理的接口
     */
    private final Class<T> interfaceClass;

    /**
     * 接口的实现类 beanNameList
     */
    private final List<String> beanNameList;

    public RoutingBeanFactoryBean(final Class<T> interfaceClass, final List<String> beanNameList) {
        this.interfaceClass = interfaceClass;
        this.beanNameList = beanNameList;
    }

    @Override
    public T getObject() {
        List<T> beanList = beanNameList.stream().map(name -> applicationContext.getBean(name, interfaceClass))
                .filter(bean -> bean.getClass().isAnnotationPresent(RoutingRule.class))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(beanList)) {
            return null;
        }
        return RoutingBeanProxyFactory.createProxy(interfaceClass, beanList);
    }

    @Override
    public Class<T> getObjectType() {
        return interfaceClass;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}
