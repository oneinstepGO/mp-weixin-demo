package com.oneinstep.demo.bootdemo.repo;

import com.oneinstep.demo.bootdemo.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepo extends JpaRepository<Address, Long> {

}
