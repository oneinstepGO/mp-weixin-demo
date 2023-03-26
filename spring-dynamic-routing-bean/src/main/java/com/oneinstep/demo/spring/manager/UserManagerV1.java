package com.oneinstep.demo.spring.manager;

import com.oneinstep.demo.spring.dto.User;
import com.oneinstep.demo.spring.routing.RoutingRule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * UserManager 实现类1
 *
 * @author aaron.shaw
 * @since 2023-03-19 22:50
 **/
@Component
@Slf4j
@RoutingRule(values = "1")
public class UserManagerV1 implements UserManager {

    @Override
    public User queryUserById(long id, Integer type) {
        log.info("queryUserById from UserManagerV1");
        return new User(1, "aaron.shaw1");
    }

    @Override
    public Integer getType() {
        return 1;
    }

}
