package com.zluolan.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zluolan.reggie.entity.ShoppingCart;
import com.zluolan.reggie.mapper.ShoppingCartMapper;
import com.zluolan.reggie.service.ShoppingCartService;

import org.springframework.stereotype.Service;

@Service
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService {
}
