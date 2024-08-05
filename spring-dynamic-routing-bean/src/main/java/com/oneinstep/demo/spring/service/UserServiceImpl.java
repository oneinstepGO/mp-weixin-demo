package com.oneinstep.demo.spring.service;

import com.oneinstep.demo.spring.dto.User;
import com.oneinstep.demo.spring.manager.UserManager;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;


/**
 * 用户查询接口 实现类
 *
 * @author aaron.shaw
 * @since 2023-03-19 22:49
 **/
@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserManager manager;

//    @Resource
//    private UserManagerFactory factory;

    @Override
    public User queryUserByType(long id, int type) {
//        return factory.getUserManager(type).queryUserById(id, type);
        return manager.queryUserById(id, type);
    }

}
