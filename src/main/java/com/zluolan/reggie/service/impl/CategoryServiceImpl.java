package com.zluolan.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zluolan.reggie.common.CustomException;
import com.zluolan.reggie.entity.Category;
import com.zluolan.reggie.entity.Dish;
import com.zluolan.reggie.entity.Setmeal;
import com.zluolan.reggie.mapper.CategoryMapper;
import com.zluolan.reggie.service.CategoryService;
import com.zluolan.reggie.service.DishService;
import com.zluolan.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Autowired
    private DishService dishService;

    @Autowired
    private SetmealService setmealService;

    /**
     * 根据id删除分类，删除之前需要进行判断
     *
     * @param id
     */
    @Override
    public void remove(Long id) {
        // 一个分类可能有多个菜品，也可能有多个套餐，所以要先查询是否关联了菜品或套餐
        LambdaQueryWrapper<Dish> dishQueryWrapper = new LambdaQueryWrapper<>(); // new 查询条件对象
        // 添加查询条件
        dishQueryWrapper.eq(Dish::getCategoryId, id);
        int count1 = dishService.count(dishQueryWrapper);
//     1. 判断当前分类是否关联了菜品，如果已经关联，抛出一个业务异常
        if (count1 > 0) {
            // 如果已经关联，抛出一个业务异常
            throw new CustomException("当前分类下关联了菜品，不能删除");
        }

        // 2. 判断当前分类是否关联了套餐，如果已经关联，抛出一个业务异常
        LambdaQueryWrapper<Setmeal> setmealQueryWrapper = new LambdaQueryWrapper<>();
        setmealQueryWrapper.eq(Setmeal::getCategoryId, id);
        int count2 = setmealService.count(setmealQueryWrapper);
        if (count2 > 0) {
            // 如果已经关联，抛出一个业务异常
            throw new CustomException("当前分类下关联了套餐，不能删除");
        }
        // 3. 正常删除分类
        super.removeById(id);

    }

}
