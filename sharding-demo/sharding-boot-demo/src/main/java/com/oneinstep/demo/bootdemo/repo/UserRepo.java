package com.oneinstep.demo.bootdemo.repo;

import com.oneinstep.demo.bootdemo.entity.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UserRepo extends CrudRepository<User, Long> {

    User findUserByUsername(String username);

    List<User> findAll();

}
