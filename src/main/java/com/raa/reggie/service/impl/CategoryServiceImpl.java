package com.raa.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.raa.reggie.common.CustomException;
import com.raa.reggie.entity.Category;
import com.raa.reggie.entity.Dish;
import com.raa.reggie.entity.Setmeal;
import com.raa.reggie.mapper.CategoryMapper;
import com.raa.reggie.service.CategoryService;
import com.raa.reggie.service.DishService;
import com.raa.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {


    @Autowired
    private DishService dishService;

    @Autowired
    private SetmealService setmealService;

    @Override
    public void remove(Long id) {
        LambdaQueryWrapper<Dish> queryWrapper1 = new LambdaQueryWrapper<>();
        // where CategoryId == ?
        queryWrapper1.eq(Dish::getCategoryId, id);
        // select count(*) from dish;
        int count = dishService.count(queryWrapper1);
        //统一风格，不能删除则报错，不通过return传递信息
        if(count != 0){
            throw new CustomException("当前分类下关联了菜品，不能删除");
        }
        LambdaQueryWrapper<Setmeal> queryWrapper2 = new LambdaQueryWrapper<>();
        queryWrapper2.eq(Setmeal::getCategoryId, id);
        count = setmealService.count(queryWrapper2);
        if(count != 0){
            throw new CustomException("当前分类下关联了套餐，不能删除");
        }
        super.removeById(id);
    }
}
