package com.oneinstep.demo.spring.routing;

import com.oneinstep.demo.spring.config.AppConfig;
import com.oneinstep.demo.spring.dto.User;
import com.oneinstep.demo.spring.service.UserService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

/**
 * @author aaron.shaw
 * @since 2023-03-26 22:12
 **/
@RunWith(SpringRunner.class)
@SpringJUnitConfig(classes = AppConfig.class)
public class RoutingBeanTest {

    @Resource
    private UserService userService;

    @Test
    public void test() {
        User user = userService.queryUserByType(1L, 1);
        Assert.assertNotNull(user);
        Assert.assertEquals(1, (int) user.getId());
        Assert.assertEquals("aaron.shaw1", user.getName());
    }
}
