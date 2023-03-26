package com.oneinstep.demo.spring.service;

import com.oneinstep.demo.spring.dto.User;

/**
 * 用户查询接口
 *
 * @author aaron.shaw
 * @since 2023-03-19 22:47
 **/
public interface UserService {

    /**
     * 根据查询类型和用户ID查询用户
     *
     * @param id   用户ID
     * @param type 查询类型
     * @return 用户信息
     */
    User queryUserByType(long id, int type);

}
