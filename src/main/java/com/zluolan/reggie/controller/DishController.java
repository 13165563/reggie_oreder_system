package com.zluolan.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zluolan.reggie.common.R;
import com.zluolan.reggie.dto.DishDto;
import com.zluolan.reggie.entity.Category;
import com.zluolan.reggie.entity.Dish;
import com.zluolan.reggie.entity.DishFlavor;
import com.zluolan.reggie.service.CategoryService;
import com.zluolan.reggie.service.DishFlavorService;
import com.zluolan.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 菜品管理
 */

@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {
    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 菜单关联分页查询
     *
     * @param page     当前页码
     * @param pageSize 每页数据量
     * @param name     菜品名称（模糊查询）
     * @return 分页结果（包含分类名称的 DishDto 列表）
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        log.info("page = {},pageSize = {},name = {}", page, pageSize, name);
        // 初始化分页参数：当前页码为 page，每页数据量为 pageSize
        Page<Dish> pageInfo = new Page(page, pageSize);
        // 初始化目标分页对象（用于最终返回的 DTO 分页结果）
        Page<DishDto> dishDtoPage = new Page<>();
        // 条件构造器：动态生成 WHERE 子句
        LambdaQueryWrapper<Dish> dishPage = new LambdaQueryWrapper();
        // 添加名称模糊查询条件（仅当 name 不为空时生效）
        dishPage.like(StringUtils.isNotEmpty(name), Dish::getName, name)

                .eq(Dish::getIsDeleted, 0) // 新增：过滤未删除的数据

                .orderByDesc(Dish::getUpdateTime);
        // 按更新时间降序排序（最新修改的数据排在最前面）
        dishPage.orderByDesc(Dish::getUpdateTime);
        // 执行分页查询，查询结果会填充到 pageInfo 对象中
        dishService.page(pageInfo, dishPage);

        // 将 pageInfo 的基础分页属性（如 total、size、current 等）拷贝到 dishDtoPage，但排除 records 字段
        // 因为 records 是 Dish 列表，需手动转换为 DishDto 列表
        BeanUtils.copyProperties(pageInfo, dishDtoPage, "records"); // 不拷贝records, 我们要封装的泛型是Page<DishDto>
        // Page里面, 当前records<Dish>列表, 是每一页的数据, 一共pagesize个数据, 所以需要手动封装到records<DishDto>列表
        List<Dish> records = pageInfo.getRecords();
        List<DishDto> list = records.stream().map((item) -> { // 拷贝records里面的基本数据，实现类型转换, 顺带添加分类名称
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);// 拷贝基本数据
            Long categoryId = item.getCategoryId(); // 分类id
            // 根据菜品关联的分类ID查询分类名称，补充到 DishDto 中
            Category category = categoryService.getById(categoryId);
            if (category != null) {
                String categoryName = category.getName(); // 分类名称
                dishDto.setCategoryName(categoryName); // 设置菜品名称
            }
            return dishDto;
        }).collect(Collectors.toList()); // 收集为list
        dishDtoPage.setRecords(list);


        return R.success(dishDtoPage);

    }

    /**
     * 添加菜品
     *
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto) {
        log.info("新增菜品:{}", dishDto);
        dishService.saveWithFlavor(dishDto);
        // 精确清理菜品的缓存数据
        String key = "dish_" + dishDto.getCategoryId() + "_1"; // dish_1397844391040167938_1
        redisTemplate.delete(key);
        return R.success("新增菜品成功");
    }

    /**
     * 根据id查询菜品信息和对应的口味信息
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id) {
        log.info("根据id查询菜品信息和对应的口味信息:{}", id);
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }


    /**
     * 修改菜品信息
     *
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto) {
        dishService.updateWithFlavor(dishDto);
        // 清理所有菜品的缓存数据
//        Set<String> keys = redisTemplate.keys("dish_*"); // 明确指定为 Set<String>
//        if (keys != null && !keys.isEmpty()) {
//            redisTemplate.delete(keys);
//        }
        // 精确清理菜品的缓存数据
        String key = "dish_" + dishDto.getCategoryId() + "_1"; // dish_1397844391040167938_1
        redisTemplate.delete(key);
        return R.success("修改菜品成功");
    }

    /**
     * 批量逻辑删除菜品和口味
     *
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids) {
        log.info("ids:{}", ids);
        dishService.removeWithFlavor(ids);
        return R.success("删除菜品成功");
    }

    /**
     * 批量停售起售菜品
     * <p>
     * 停售：status = 0
     * 起售：status = 1
     * <p>
     *
     * @param status
     * @param ids    多个id，以逗号分隔
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> status(@PathVariable Integer status, @RequestParam List<Long> ids) {
        log.info("批量停售起售菜品:{}", ids);
        dishService.updateStatusByIds(status, ids);
        return R.success("批量停售起售菜品成功");
    }

    /**
     * 根据条件查询菜品数据
     *
     * @param dish
     * @return
     */
//    @GetMapping("/list")
//    public R<List<Dish>> list(Dish dish) {
//        log.info("根据条件查询菜品数据:{}", dish);
//        LambdaQueryWrapper<Dish> dishQueryWrapper = new LambdaQueryWrapper<>();
//        dishQueryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId())
//                .eq(Dish::getStatus, 1)
//                .orderByAsc(Dish::getSort)
//                .orderByDesc(Dish::getUpdateTime);
//        List<Dish> dishList = dishService.list(dishQueryWrapper);
//        return R.success(dishList);
//    }


    // 查询菜品数据，追加口味数据
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish) {
        // 构造key
        String key = "dish_" + dish.getCategoryId() + "_" + dish.getStatus(); // dish_1397844391040167938_1
        List<DishDto> dishDtoList = null;
        // 先从redis中获取缓存数据
        dishDtoList = (List<DishDto>) redisTemplate.opsForValue().get(key);
        if (dishDtoList != null) {
            // 如果存在，直接返回，无需查询数据库
            return R.success(dishDtoList);
        }
        // 如果不存在，需要查询数据库，将查询到的菜品数据缓存到Redis

        // 构造条件查询菜品
        LambdaQueryWrapper<Dish> dishQueryWrapper = new LambdaQueryWrapper<>();
        dishQueryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId())
                .eq(Dish::getStatus, 1) // 启售
                .orderByAsc(Dish::getSort)
                .orderByDesc(Dish::getUpdateTime);
        List<Dish> dishList = dishService.list(dishQueryWrapper);
        // 封装菜品数据到 DishDto 列表，并添加口味信息和分类名称
        dishDtoList = dishList.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);
            Category category = categoryService.getById(item.getCategoryId()); // 根据菜品分类id查询分类名称
            if (category != null) {
                dishDto.setCategoryName(category.getName()); // 添加分类名称
            }
            // 当前菜品id
            Long dishId = item.getId();
            // 构造条件查询菜品口味
            LambdaQueryWrapper<DishFlavor> dishFlavorQueryWrapper = new LambdaQueryWrapper<>();
            dishFlavorQueryWrapper.eq(DishFlavor::getDishId, dishId);
            // SQL: select * from dish_flavor where dish_id = ?
            List<DishFlavor> flavors = dishFlavorService.list(dishFlavorQueryWrapper);
            dishDto.setFlavors(flavors);// 添加口味信息
            return dishDto; // 方法每次执行完一个流返回
        }).collect(Collectors.toList()); // 收集流为List<DishDto>

        // 将菜品数据缓存到Redis
        redisTemplate.opsForValue().set(key, dishDtoList, 60, TimeUnit.MINUTES); // dishDtoList 是 List<DishDto>

        return R.success(dishDtoList);
    }

}
