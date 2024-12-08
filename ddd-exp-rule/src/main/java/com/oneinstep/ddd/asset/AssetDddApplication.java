package com.oneinstep.ddd.asset;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class AssetDddApplication {

    public static void main(String[] args) {
        SpringApplication.run(AssetDddApplication.class, args);
    }

}
