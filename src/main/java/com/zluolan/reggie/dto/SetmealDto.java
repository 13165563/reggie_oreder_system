package com.zluolan.reggie.dto;


import com.zluolan.reggie.entity.Setmeal;
import com.zluolan.reggie.entity.SetmealDish;
import lombok.Data;

import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
