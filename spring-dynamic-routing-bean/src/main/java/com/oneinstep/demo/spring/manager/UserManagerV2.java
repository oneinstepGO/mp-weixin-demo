package com.oneinstep.demo.spring.manager;

import com.oneinstep.demo.spring.dto.User;
import com.oneinstep.demo.spring.routing.RoutingRule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * UserManager 实现类2
 *
 * @author aaron.shaw
 * @since 2023-03-19 22:51
 **/
@Component
@Slf4j
@RoutingRule(values = "2")
public class UserManagerV2 implements UserManager {

    @Override
    public User queryUserById(long id, Integer type) {
        log.info("queryUserById from UserManagerV2");
        return new User(2, "aaron.shaw2");
    }

    @Override
    public Integer getType() {
        return 2;
    }

}
