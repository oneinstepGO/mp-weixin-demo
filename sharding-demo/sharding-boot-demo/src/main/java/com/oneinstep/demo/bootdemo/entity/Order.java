package com.oneinstep.demo.bootdemo.entity;

import jakarta.persistence.*;
import lombok.Data;


@Entity
@Data
@Table(name = "t_order")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    private String orderName;

    private Long userId;

}
