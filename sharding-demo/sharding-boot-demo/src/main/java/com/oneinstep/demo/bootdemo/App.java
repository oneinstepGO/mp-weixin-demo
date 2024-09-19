package com.oneinstep.demo.bootdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.concurrent.TimeUnit;

@SpringBootApplication
@EnableTransactionManagement
public class App {

    public static void main(String[] args) {
        try {
            // wait for MySQL to init data
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        SpringApplication.run(App.class, args);
    }

}
