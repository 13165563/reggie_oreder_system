package com.zluolan.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zluolan.reggie.dto.DishDto;
import com.zluolan.reggie.entity.Dish;

import java.util.List;


public interface DishService extends IService<Dish> {

    // 新增菜品，同时保存对应的口味数据，dishDto包含菜品数据，以及口味数据
    public void saveWithFlavor(DishDto dishDto);

    // 根据id查询菜品信息和对应的口味信息
    DishDto getByIdWithFlavor(Long id);

    // 更新菜品信息，同时更新对应的口味数据，dishDto包含菜品数据，以及口味数据
    void updateWithFlavor(DishDto dishDto);

    void removeWithFlavor(List<Long> ids);

    void updateStatusByIds(Integer status, List<Long> ids);
}
