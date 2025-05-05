package com.zluolan.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zluolan.reggie.common.R;
import com.zluolan.reggie.entity.Orders;
import com.zluolan.reggie.service.OrdersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.*;

/**
 * 订单
 */

@Slf4j
@RestController
@RequestMapping("/order")
public class OrdersController {

    @Autowired
    private OrdersService ordersService;

    /**
     * 用户下单
     *
     * @param orders 请求数据是json，orders里面都有了
     * @return
     */

    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders) {
        ordersService.submit(orders);
        return R.success("下单成功");
    }

    /**
     * 订单分页查询
     *
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/userPage")
    public R<Page> page(int page, int pageSize) {
        log.info("page = {},pageSize = {}", page, pageSize);
        // 初始化分页参数：当前页码为 page，每页数据量为 pageSize
        Page<Orders> pageInfo = new Page(page, pageSize);
        // 初始化目标分页对象（用于最终返回的 DTO 分页结果）
        Page<Orders> ordersPage = new Page<>();
        // 条件构造器：动态生成 WHERE 子句
        LambdaQueryWrapper<Orders> ordersPageWrapper = new LambdaQueryWrapper();
        ordersPageWrapper.orderByDesc(Orders::getOrderTime);
        // 执行分页查询，查询结果会填充到 pageInfo 对象中
        ordersService.page(pageInfo, ordersPageWrapper);
        return R.success(pageInfo);
    }

}
