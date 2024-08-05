package com.oneinstep.demo.spring;

import com.oneinstep.demo.spring.config.AppConfig;
import com.oneinstep.demo.spring.manager.UserManager;
import jakarta.annotation.Resource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author aaron.shaw
 * @since 2023-03-19 22:53
 **/
@RunWith(SpringRunner.class)
@SpringJUnitConfig(classes = AppConfig.class)
public class UserManagerErrorTest {

    @Resource
    private UserManager userManager;

    @Test(expected = NoUniqueBeanDefinitionException.class)
    public void test() {
        userManager.queryUserById(1L, 1);
    }

}
