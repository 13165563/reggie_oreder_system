package com.zluolan.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zluolan.reggie.dto.SetmealDto;
import com.zluolan.reggie.entity.Setmeal;
import com.zluolan.reggie.entity.SetmealDish;
import com.zluolan.reggie.mapper.SetmealMapper;
import com.zluolan.reggie.service.SetmealDishService;
import com.zluolan.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    /**
     * 删除套餐，同时需要删除套餐和菜品的关联数据
     * 1. 查询套餐状态，确定是否可用删除
     * 2. 删除套餐表中的数据---setmeal
     * 3. 删除关系表中的数据---setmeal_dish
     *
     * @param ids
     * @return
     * @author zluolan
     */
    @Override
    public void removeWithDish(List<Long> ids) {
        //查询套餐状态，确定是否可用删除
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId, ids);
        queryWrapper.eq(Setmeal::getStatus, 1);
        // SQL: select count(*) from setmeal where id in (1,2,3) and status = 1;
        int count = this.count(queryWrapper);
        if (count > 0) {
            //如果不能删除，抛出一个业务异常
            throw new RuntimeException("套餐正在售卖中，不能删除");
        }
        //如果可以删除，先删除套餐表中的数据---setmeal
        this.removeByIds(ids);
        // 再删除关系表中的数据---setmeal_dish
        // SQL: delete from setmeal_dish where setmeal_id in (1,2,3)
        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(SetmealDish::getId, ids);
        setmealDishService.remove(lambdaQueryWrapper);


    }
}
