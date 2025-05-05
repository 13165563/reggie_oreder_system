package com.zluolan.reggie;

import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@EnableTransactionManagement //     开启事务管理
@Slf4j
@SpringBootApplication
//过滤器扫描
@ServletComponentScan
//@MapperScan("com.zluolan.reggie.mapper")  // 确保包路径与EmployeeMapper实际位置一致
public class ReggieOrderSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(ReggieOrderSystemApplication.class, args);
        log.info("项目启动成功...");
        long id = Thread.currentThread().getId();
        log.info("当前线程id为：{}", id);
    }
}