package com.oneinstep.demo.spring.manager;

import com.oneinstep.demo.spring.dto.User;
import com.oneinstep.demo.spring.routing.RoutingBean;
import com.oneinstep.demo.spring.routing.RoutingKey;

/**
 * UserManager 接口
 *
 * @author aaron.shaw
 * @since 2023-03-19 22:50
 **/
@RoutingBean
public interface UserManager {

    /**
     * 根据ID 查询用户
     *
     * @param id 用户ID
     * @param type 类型
     * @return 用户信息
     */
    User queryUserById(long id, @RoutingKey Integer type);

    /**
     * 该 manager 支持的类型
     *
     * @return 类型
     */
    Integer getType();

}
