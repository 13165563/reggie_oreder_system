package com.zluolan.reggie.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 员工实体
 */
@Data
public class Employee implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String username;

    private String name;

    private String password;

    private String phone;

    private String sex;

    private String idNumber;//身份证号码

    private Integer status;

    // TableField注解里面的fill属性是用来指定字段的填充方式，默认为空，即不填充
    @TableField(fill = FieldFill.INSERT) // 插入时自动填充
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE) // 插入和更新时自动填充
    private LocalDateTime updateTime;

    @TableField(fill = FieldFill.INSERT) // 插入时自动填充
    private Long createUser;

    @TableField(fill = FieldFill.INSERT_UPDATE) // 插入和更新时自动填充
    private Long updateUser;

}
