package com.zluolan.reggie;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.zluolan.reggie.mapper")  // 确保包路径与EmployeeMapper实际位置一致
public class ReggieOrderSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(ReggieOrderSystemApplication.class, args);
    }
}