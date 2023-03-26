package com.oneinstep.demo.spring.routing;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * BeanFactory 后置处理器
 * 定义新的Bean
 * @author aaron.shaw
 * @since 2023-03-26 20:41
 **/
@Component
public class RoutingBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {
        Map<Class<?>, List<String>> routerInterfaceMap = new HashMap<>(8);
        String[] beanNames = configurableListableBeanFactory.getBeanNamesForAnnotation(RoutingBean.class);
        for (String beanName : beanNames) {
            Class<?> clazz = configurableListableBeanFactory.getType(beanName);
            if (clazz == null) {
                continue;
            }
            for (Class<?> interfaceClass : clazz.getInterfaces()) {
                if (interfaceClass.getAnnotation(RoutingBean.class) != null) {
                    routerInterfaceMap.computeIfAbsent(interfaceClass, k -> new ArrayList<>()).add(beanName);
                }
            }
        }
        DefaultListableBeanFactory registry = (DefaultListableBeanFactory) configurableListableBeanFactory;
        routerInterfaceMap.forEach((interfaceClass, beanNameList) -> {
            if (CollectionUtils.isEmpty(beanNameList)) {
                return;
            }
            BeanDefinition beanDefinition = BeanDefinitionBuilder.genericBeanDefinition(RoutingBeanFactoryBean.class)
                    .setScope(BeanDefinition.SCOPE_SINGLETON)
                    .addConstructorArgValue(interfaceClass)
                    .addConstructorArgValue(beanNameList)
                    .getBeanDefinition();
            String interfaceName = interfaceClass.getSimpleName();
            interfaceName = interfaceName.substring(0, 1).toLowerCase() + interfaceName.substring(1);
            registry.registerBeanDefinition(interfaceName, beanDefinition);
            // 必须设置 被@RouterInterface 注解标注的接口的实现类 对应的 bean 为非Autowire候选人 setAutowireCandidate(false)，
            // 否则使用 @Autowired 或者 @Resources 注解注入被 @RouterInterface 注解的接口的时候会报找到多个bean 错误
            beanNameList.forEach(name -> registry.getBeanDefinition(name).setAutowireCandidate(false));
        });
    }

}
