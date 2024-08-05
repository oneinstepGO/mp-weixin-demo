package com.oneinstep.demo.spring;

import com.oneinstep.demo.spring.config.AppConfig;
import com.oneinstep.demo.spring.dto.User;
import com.oneinstep.demo.spring.service.UserService;
import jakarta.annotation.Resource;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * UserService 测试类
 *
 * @author aaron.shaw
 * @since 2023-03-19 22:53
 **/
@RunWith(SpringRunner.class)
@SpringJUnitConfig(classes = AppConfig.class)
public class UserServiceTest {

    @Resource
    private UserService userService;

    @Test
    public void test() {
        User user = userService.queryUserByType(1L, 1);
        Assert.assertNotNull(user);
        Assert.assertEquals(1, (int) user.getId());
        Assert.assertEquals("aaron.shaw1", user.getName());

        User user2 = userService.queryUserByType(1L, 2);
        Assert.assertNotNull(user);
        Assert.assertEquals(2, (int) user2.getId());
        Assert.assertEquals("aaron.shaw2", user2.getName());
    }

}
