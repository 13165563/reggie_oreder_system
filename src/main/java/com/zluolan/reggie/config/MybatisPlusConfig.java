package com.zluolan.reggie.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 配置MP的分页插件
 */
@Configuration // 配置类注解
public class MybatisPlusConfig {

    @Bean // 创建一个MP的分页插件对象，交给spring管理
    public MybatisPlusInterceptor mybatisPlusInterceptor(){
        MybatisPlusInterceptor mybatisPlusInterceptor = new MybatisPlusInterceptor(); // 创建拦截器对象
        mybatisPlusInterceptor.addInnerInterceptor(new PaginationInnerInterceptor()); // 添加分页拦截器
        return mybatisPlusInterceptor;
    }
}
