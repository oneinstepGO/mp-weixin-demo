package com.oneinstep.demo.bootdemo.entity;

import jakarta.persistence.*;
import lombok.Data;


@Entity
@Data
@Table(name = "t_address")
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String province;

    private String city;

    private String area;

    private String detail;

}
