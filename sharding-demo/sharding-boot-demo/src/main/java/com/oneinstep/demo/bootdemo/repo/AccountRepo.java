package com.oneinstep.demo.bootdemo.repo;

import com.oneinstep.demo.bootdemo.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepo extends JpaRepository<Account, Long> {

}
