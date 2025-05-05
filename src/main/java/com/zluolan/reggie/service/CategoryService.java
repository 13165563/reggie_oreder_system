package com.zluolan.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zluolan.reggie.entity.Category;

public interface CategoryService extends IService<Category> {
    void remove(Long id);

}