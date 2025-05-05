package com.zluolan.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zluolan.reggie.common.R;
import com.zluolan.reggie.dto.DishDto;
import com.zluolan.reggie.dto.SetmealDto;
import com.zluolan.reggie.entity.Category;
import com.zluolan.reggie.entity.Dish;
import com.zluolan.reggie.entity.Setmeal;
import com.zluolan.reggie.service.CategoryService;
import com.zluolan.reggie.service.SetmealDishService;
import com.zluolan.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zluolan
 * @date 2023/5/7-20:05
 */

/**
 * 套餐管理
 */


@Slf4j
@RestController
@RequestMapping("/setmeal")
public class SetmealController {
    @Autowired
    private SetmealService setmealService;
    @Autowired
    private SetmealDishService setmealDishService;
    @Autowired
    private CategoryService categoryService;

    /**
     * 分页查询套餐
     *
     * @param page
     * @param pageSize
     * @param name     - 套餐名称模糊查询
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        log.info("page = {},pageSize = {},name = {}", page, pageSize, name);
        // 初始化分页参数：当前页码为 page，每页数据量为 pageSize, <>表示对目标对象进行分页操作
        Page<Setmeal> pageInfo = new Page(page, pageSize);
        // 初始化目标分页对象（用于最终返回的 DTO 分页结果）
        Page<SetmealDto> dishDtoPage = new Page<>();


        // 条件构造器：动态生成 WHERE 子句
        LambdaQueryWrapper<Setmeal> dishPage = new LambdaQueryWrapper();
        // 添加名称模糊查询条件（仅当 name 不为空时生效）
        dishPage.like(StringUtils.isNotEmpty(name), Setmeal::getName, name);
        // 添加排序条件：按照更新时间降序排序
        dishPage.orderByDesc(Setmeal::getUpdateTime);
        // 执行分页查询，将查询结果封装到 pageInfo 中
        setmealService.page(pageInfo, dishPage);

        /**
         *分页相关结果信息会封装到 Setmeal类型的 pageInfo 对象中，pageInfo 是分页对象，里面的 records 是分页查询结果
         * 返回要SetmealDto类型的分页结果，顺带添加套餐分类名称
         */
        // 复制分页基础信息到 dishDtoPage 对象中, 排除分页查询结果信息
        BeanUtils.copyProperties(pageInfo, dishDtoPage, "records");
        // 获取原始分页查询结果中的 records 列表，这是 Setmeal 对象的列表
        List<Setmeal> records = pageInfo.getRecords();
        // 使用流操作对 records 列表进行处理，转位为 SetmealDto 对象列表
        List<SetmealDto> list = records.stream().map((item) -> {
            // 创建 SetmealDto 对象
            SetmealDto dishDto = new SetmealDto();
            BeanUtils.copyProperties(item, dishDto); // 拷贝属性
            // 根据套餐的分类 ID 查询分类名称
            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if (category != null) {
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName); // 添加分类名称到 dishDto 对象中
            }
            return dishDto; // 返回处理后的 dishDto 对象
        }).collect(Collectors.toList()); // 收集为 list
        // 将处理后的结果设置到 dishDtoPage 对象中
        dishDtoPage.setRecords(list);
        return R.success(dishDtoPage);
    }

    /**
     * 新增套餐，添加套餐时，同时添加套餐和菜品的关联关系
     *
     * @param setmealDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto) {
        log.info("套餐信息：{}", setmealDto);
        setmealService.saveWithDish(setmealDto);
        return R.success("新增套餐成功");
    }


    /**
     * 根据条件查询套餐信息
     *
     * @param setmeal
     * @return
     */
    @GetMapping("/list")
    public R<List<Setmeal>> get(Setmeal setmeal) {
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(setmeal.getCategoryId() != null, Setmeal::getCategoryId, setmeal.getCategoryId())
                .eq(setmeal.getStatus() != null, Setmeal::getStatus, setmeal.getStatus())
                .orderByDesc(Setmeal::getUpdateTime);
        List<Setmeal> list = setmealService.list(queryWrapper);
        return R.success(list);
    }

}
