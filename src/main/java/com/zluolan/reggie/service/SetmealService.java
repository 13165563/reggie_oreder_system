package com.zluolan.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zluolan.reggie.dto.SetmealDto;
import com.zluolan.reggie.entity.Setmeal;


public interface SetmealService extends IService<Setmeal> {
    void saveWithDish(SetmealDto setmealDto);
}
