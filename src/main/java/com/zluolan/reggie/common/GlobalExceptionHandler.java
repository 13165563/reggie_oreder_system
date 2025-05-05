package com.zluolan.reggie.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理
 */
@ControllerAdvice(annotations = {RestController.class, Controller.class})
// annotations指定扫描范围，只扫描有@RestController或@Controller注解的类
@ResponseBody // 返回json数据
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 异常处理方法
     *
     * @return
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    // 捕获SQLIntegrityConstraintViolationException异常，一旦捕获到异常，执行此方法
    public R<String> exceptionHandler(SQLIntegrityConstraintViolationException ex) {
        log.error(ex.getMessage());
        //  c.z.r.common.GlobalExceptionHandler     异常信息： Duplicate entry 'zluolan' for key 'idx_username'
        // 包含Duplicate entry这些字段，则表示用户名已存在
        if (ex.getMessage().contains("Duplicate entry")) {
            String[] split = ex.getMessage().split(" "); // 按照空格分数组，split[2]表示用户名
            String msg = split[2] + "已存在";
            return R.error(msg);
        }

        return R.error("未知错误");
    }

    /**
     * 捕获业务异常
     *
     * @return
     */
    @ExceptionHandler(CustomException.class)
    public R<String> exceptionHandler(CustomException ex) {
        log.error(ex.getMessage());
        return R.error(ex.getMessage());
    }  // 捕获CustomException异常，一旦捕获到异常，执行此方法

}
