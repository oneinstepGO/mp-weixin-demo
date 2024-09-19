package com.oneinstep.demo.bootdemo.entity;

import jakarta.persistence.*;
import lombok.Data;



@Entity
@Data
@Table(name = "t_order_item")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderItemId;

    private Long userId;

    private Long orderId;

    private String itemName;
}
