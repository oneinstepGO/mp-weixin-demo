package com.oneinstep.demo.bootdemo.repo;

import com.oneinstep.demo.bootdemo.entity.OrderItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface OrderItemRepo extends CrudRepository<OrderItem, Long> {

    List<OrderItem> findAll();

    Page<OrderItem> findOrderItemsByOrderId(Long orderId, PageRequest pageRequest);

}
