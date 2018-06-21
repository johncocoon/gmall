package com.atguigu.gmall1108.llistweb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan("com.atguigu.gmall1108")
@SpringBootApplication
public class GmallListWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(GmallListWebApplication.class, args);
    }
}
