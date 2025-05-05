package com.zluolan.reggie.dto;


import com.zluolan.reggie.dto.DishDto;
import com.zluolan.reggie.dto.DishDto;
import com.zluolan.reggie.entity.Dish;
import com.zluolan.reggie.entity.DishFlavor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 原本的dish不够用了，对dish类进行扩展
 */
@Data
public class DishDto extends Dish {

    private List<DishFlavor> flavors = new ArrayList<>(); // 菜品口味

    private String categoryName;

    private Integer copies;
}
