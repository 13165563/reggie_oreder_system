package com.zluolan.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zluolan.reggie.dto.SetmealDto;
import com.zluolan.reggie.entity.Setmeal;
import com.zluolan.reggie.mapper.SetmealMapper;
import com.zluolan.reggie.service.SetmealDishService;
import com.zluolan.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;


    /**
     * 新增套餐，同时需要保存套餐和菜品的关联关系
     *
     * @param setmealDto
     * @return
     * @author zluolan
     */
    @Override
    @Transactional
    public void saveWithDish(SetmealDto setmealDto) {
        //保存套餐的基本信息，操作setmeal，执行insert操作
        this.save(setmealDto);
        // 返回的值没有setmealId, 所以需要手动设置setmealId
        Long setmealId = setmealDto.getId();
        setmealDto.getSetmealDishes().forEach(dish -> {
            dish.setSetmealId(setmealId);
        });
        /*
         * 或者使用stream流的方式
         * List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
         *  setmealDishes.stream().map(dish -> {
         *     dish.setSetmealId(setmealId);}).collect(Collectors.toList());
         * */

        //保存套餐和菜品的关联信息，操作setmeal_dish，执行insert操作
        setmealDishService.saveBatch(setmealDto.getSetmealDishes());
    }
}
