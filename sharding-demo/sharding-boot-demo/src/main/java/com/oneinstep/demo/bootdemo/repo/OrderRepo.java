package com.oneinstep.demo.bootdemo.repo;

import com.oneinstep.demo.bootdemo.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepo extends JpaRepository<Order, Long> {

}
