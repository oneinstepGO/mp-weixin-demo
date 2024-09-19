package com.oneinstep.demo.bootdemo.entity;

import jakarta.persistence.*;
import lombok.Data;


@Entity
@Data
@Table(name = "t_user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    private String username;

    private String password;

    private String email;

    private String telephone;
}
