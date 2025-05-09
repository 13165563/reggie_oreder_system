package com.zluolan.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zluolan.reggie.dto.SetmealDto;
import com.zluolan.reggie.entity.Setmeal;

import java.util.List;


public interface SetmealService extends IService<Setmeal> {
    void saveWithDish(SetmealDto setmealDto);

    void removeWithDish(List<Long> ids);
}
