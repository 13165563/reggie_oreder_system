package com.zluolan.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zluolan.reggie.dto.DishDto;
import com.zluolan.reggie.entity.Dish;
import com.zluolan.reggie.entity.DishFlavor;
import com.zluolan.reggie.mapper.DishMapper;
import com.zluolan.reggie.service.DishFlavorService;
import com.zluolan.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;

    /**
     * 新增菜品，同时保存对应的口味数据
     *
     * @param dishDto
     * @return
     */
    @Transactional //事务注解，保证数据的一致性，要么都成功，要么都失败
    @Override
    public void saveWithFlavor(DishDto dishDto) {
        //保存菜品的基本信息到菜品表dish
        this.save(dishDto);
        // 根据ID，保存菜品口味数据到菜品口味表dish_flavor
        Long dishId = dishDto.getId();//菜品id
        dishDto.getFlavors().stream().map((item) -> {
            item.setDishId(dishId);
            return item;
        }).forEach(dishFlavorService::save); // 菜品口味数据保存到菜品口味表dish_flavor

        // 或者collect
        // List<DishFlavor> flavors = dishDto.getFlavors();
        // flavors.stream().map((item) -> {
        //     item.setDishId(dishId);
        //     return item;
        //     }).collect(Collectors.toList());
        // dishFlavorService.saveBatch(flavors);

    }

    /**
     * 根据id查询菜品信息和对应的口味信息
     *
     * @param id
     * @return
     */
    @Override
    public DishDto getByIdWithFlavor(Long id) {
        //查询菜品基本信息，从dish表查询
        Dish dish = this.getById(id);
        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish, dishDto);
        //查询当前菜品对应的口味信息，从dish_flavor表查询
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, dish.getId());
        List<DishFlavor> flavors = dishFlavorService.list(queryWrapper);
        dishDto.setFlavors(flavors);
        return dishDto;
    }

    /**
     * 更新菜品信息，同时更新对应的口味信息
     *
     * @param dishDto
     * @return
     */
    @Override
    public void updateWithFlavor(DishDto dishDto) {
        // 更新dish表基本信息
        this.updateById(dishDto);
        //清理当前菜品对应口味数据---dish_flavor表的delete操作
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, dishDto.getId());
        dishFlavorService.remove(queryWrapper);
        //添加当前提交过来的口味数据---dish_flavor表的insert操作
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors.stream().map((item) -> {
            item.setDishId(dishDto.getId());
            return item;
        }).forEach(dishFlavorService::save);

        // 或者collect
        // List<DishFlavor> flavors = dishDto.getFlavors();
        // flavors.stream().map((item) -> {
        //     item.setDishId(dishDto.getId());//     item.setDishId(dishDto.getId());
        //     return item;
        //     }).collect(Collectors.toList());
    }

    /**
     * 根据id逻辑删除菜品信息，同时逻辑删除对应的口味信息
     *
     * @param ids
     * @return
     */
    @Override
    @Transactional
    public void removeWithFlavor(List<Long> ids) {
        // 1. 逻辑删除菜品
        LambdaUpdateWrapper<Dish> dishWrapper = new LambdaUpdateWrapper<>();
        dishWrapper.in(Dish::getId, ids)
                .set(Dish::getIsDeleted, 1);
        this.update(dishWrapper);

        // 2. 逻辑删除关联的口味
        LambdaUpdateWrapper<DishFlavor> flavorWrapper = new LambdaUpdateWrapper<>();
        flavorWrapper.in(DishFlavor::getDishId, ids)
                .set(DishFlavor::getIsDeleted, 1);
        dishFlavorService.update(flavorWrapper);


    }

    /**
     * 批量修改菜品状态
     *
     * @param status
     * @param ids
     */
    @Override
    public void updateStatusByIds(Integer status, List<Long> ids) {
        UpdateWrapper<Dish> updateWrapper = new UpdateWrapper<>();
        updateWrapper.in("id", ids)
                .set("status", status);
        this.update(updateWrapper);
    }

}
