package com.zluolan.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zluolan.reggie.common.BaseContext;
import com.zluolan.reggie.common.CustomException;
import com.zluolan.reggie.entity.*;
import com.zluolan.reggie.mapper.OrdersMapper;
import com.zluolan.reggie.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrdersService {

    @Autowired
    private ShoppingCartService shoppingCartService;
    @Autowired
    private UserService userService;
    @Autowired
    private AddressBookService addressBookService;

    @Autowired
    private OrderDetailService orderDetailService;

    /**
     * 用户下单
     *
     * @param orders
     */
    @Override
    @Transactional
    public void submit(Orders orders) {
        // 知道当前用户是谁
        Long userId = BaseContext.getCurrentId(); // 拿到用户id
        // 查询当前用户的购物车数据
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId);
        List<ShoppingCart> shoppingCartList = shoppingCartService.list(queryWrapper);
        if (shoppingCartList == null || shoppingCartList.size() == 0) {
            throw new CustomException("购物车为空，不能下单");
        }
        // 查询用户和地址信息
        User user = userService.getById(userId);// 拿到用户信息
        Long addressBookId = orders.getAddressBookId();// 拿到地址id
        AddressBook addressBook = addressBookService.getById(addressBookId);// 查询地址信息
        if (addressBook == null) {
            throw new CustomException("地址信息有误，不能下单");
        }
        // MybatisPlus的IdworkId,生成订单号
        long orderId = IdWorker.getId();

        // 计算总金额
        AtomicInteger amount = new AtomicInteger(0);
        List<OrderDetail> orderDetails = shoppingCartList.stream().map((item) -> {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(orderId);
            orderDetail.setNumber(item.getNumber());
            orderDetail.setDishId(item.getDishId());
            orderDetail.setSetmealId(item.getSetmealId());
            orderDetail.setName(item.getName());
            orderDetail.setImage(item.getImage());
            orderDetail.setAmount(item.getAmount());
            amount.addAndGet(item.getAmount().multiply(new BigDecimal(item.getNumber())).intValue());
            return orderDetail;

        }).collect(Collectors.toList());

        // 向订单表插入一条数据
        orders.setId(orderId);
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setStatus(2);
        orders.setAmount(new BigDecimal(amount.get())); // 总金额
        orders.setUserId(userId);
        orders.setNumber(String.valueOf(orderId)); // 设置订单号
        orders.setUserName(user.getName());
        orders.setConsignee(addressBook.getConsignee());
        orders.setPhone(addressBook.getPhone());
        orders.setAddress((addressBook.getProvinceName() == null ? "" : addressBook.getProvinceName())
                + (addressBook.getCityName() == null ? "" : addressBook.getCityName())
                + (addressBook.getDistrictName() == null ? "" : addressBook.getDistrictName()) + (addressBook.getDistrictName() == null ? "" : addressBook.getDistrictName())
                + (addressBook.getDetail() == null ? "" : addressBook.getDetail()));

        this.save(orders);
        // 向订单明细表插入多条数据
        orderDetailService.saveBatch(orderDetails);
        // 清空购物车数据
        shoppingCartService.remove(queryWrapper);
    }

}
