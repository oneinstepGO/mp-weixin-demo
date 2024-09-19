package com.oneinstep.demo.bootdemo.entity;

import jakarta.persistence.*;
import lombok.Data;


@Entity
@Data
@Table(name = "t_account")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    private String username;
}
