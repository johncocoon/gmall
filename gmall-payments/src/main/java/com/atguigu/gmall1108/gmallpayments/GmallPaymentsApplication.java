package com.atguigu.gmall1108.gmallpayments;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;

@ComponentScan("com.atguigu.gmall1108")
@MapperScan("com.atguigu.gmall1108.gmallpayments.mapper")
@SpringBootApplication
public class GmallPaymentsApplication {

    public static void main(String[] args) {
        SpringApplication.run(GmallPaymentsApplication.class, args);
    }
}
