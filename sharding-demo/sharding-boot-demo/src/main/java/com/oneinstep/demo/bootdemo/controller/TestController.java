package com.oneinstep.demo.bootdemo.controller;

import com.oneinstep.demo.bootdemo.entity.Account;
import com.oneinstep.demo.bootdemo.entity.Order;
import com.oneinstep.demo.bootdemo.entity.OrderItem;
import com.oneinstep.demo.bootdemo.repo.AccountRepo;
import com.oneinstep.demo.bootdemo.repo.OrderItemRepo;
import com.oneinstep.demo.bootdemo.repo.OrderRepo;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/test")
public class TestController {

    @Resource
    private AccountRepo accountRepo;
    @Resource
    private OrderRepo orderRepo;
    @Resource
    private OrderItemRepo orderItemRepo;

    @GetMapping("addAccountAndOrder")
    public String addAccountAndOrder() {
        Account account = new Account();
        account.setUsername("test");
        Account saveAccount = accountRepo.save(account);

        Order order = new Order();
        order.setOrderName("test");
        order.setUserId(saveAccount.getUserId());
        Order save = orderRepo.save(order);

        OrderItem orderItem1 = new OrderItem();
        orderItem1.setOrderId(save.getOrderId());
        orderItem1.setUserId(saveAccount.getUserId());
        orderItem1.setItemName("test1");
        orderItemRepo.save(orderItem1);

        OrderItem orderItem2 = new OrderItem();
        orderItem2.setOrderId(save.getOrderId());
        orderItem2.setUserId(saveAccount.getUserId());
        orderItem2.setItemName("test2");
        orderItemRepo.save(orderItem2);

        return "success";
    }

    @GetMapping("/users")
    public List<Account> getUsers() {
        return accountRepo.findAll();
    }

    @GetMapping("/user/{userId}")
    public Account getUser(@PathVariable Long userId) {
        return accountRepo.findById(userId).orElse(null);
    }

    @GetMapping("/orders")
    public List<Order> getOrders() {
        return orderRepo.findAll();
    }

    @GetMapping("/order/{orderId}")
    public Order getOrder(@PathVariable Long orderId) {
        return orderRepo.findById(orderId).orElse(null);
    }

    @GetMapping("/orderItems")
    public List<OrderItem> getOrderItems() {
        return orderItemRepo.findAll();
    }

    @GetMapping("/orderItem/{orderItemId}")
    public OrderItem getOrderItem(@PathVariable Long orderItemId) {
        return orderItemRepo.findById(orderItemId).orElse(null);
    }

    // 测试分页
    @GetMapping("/orderItems/{orderId}/{page}/{size}")
    public Page<OrderItem> getOrderItems(@PathVariable Long orderId, @PathVariable int page, @PathVariable int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        return orderItemRepo.findOrderItemsByOrderId(orderId, pageRequest);
    }
}
