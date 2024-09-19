package com.oneinstep.demo.bootdemo.entity;

import jakarta.persistence.*;
import lombok.Data;


@Entity
@Data
@Table(name = "t_address")
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

}
