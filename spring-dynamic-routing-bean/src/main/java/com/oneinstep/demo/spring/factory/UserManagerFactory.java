package com.oneinstep.demo.spring.factory;

import com.oneinstep.demo.spring.manager.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * UserManager 工厂
 *
 * @author aaron.shaw
 * @since 2023-03-26 12:22
 **/
//@Component
public class UserManagerFactory {

    private final List<UserManager> userManagers;

    @Autowired
    private UserManagerFactory(List<UserManager> userManagers) {
        this.userManagers = userManagers;
    }

    private static final Map<Integer, UserManager> USER_MANAGER_MAP = new HashMap<>();

    @PostConstruct
    public void init() {
        for (UserManager userManager : userManagers) {
            USER_MANAGER_MAP.put(userManager.getType(), userManager);
        }
    }


    public UserManager getUserManager(int type) {
        return USER_MANAGER_MAP.get(type);
    }

}
