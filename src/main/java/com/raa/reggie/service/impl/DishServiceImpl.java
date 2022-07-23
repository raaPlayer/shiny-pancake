package com.raa.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.raa.reggie.dto.DishDto;
import com.raa.reggie.entity.Dish;
import com.raa.reggie.entity.DishFlavor;
import com.raa.reggie.mapper.DishMapper;
import com.raa.reggie.service.DishFlavorService;
import com.raa.reggie.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;

    //Transactional多张表操作,要开启事务,确保全部正确报存,即多次操作使用同一个SqlSession
    @Transactional
    public void saveWithFlavor(DishDto dishDto) {
        this.save(dishDto);
        Long dishId = dishDto.getId();
        List<DishFlavor> flavor = dishDto.getFlavors();
//        flavor = flavor.stream().map((item) -> {
//            item.setDishId(dishId);
//            return item;
//        }).collect(Collectors.toList());
        flavor = flavor.stream()
                .peek((item) -> item.setDishId(dishId))
                .collect(Collectors.toList());
        dishFlavorService.saveBatch(flavor);
    }

    public DishDto getByIdWithFlavor(Long id) {
        Dish dish = this.getById(id);
        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish, dishDto);

        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, id);
        List<DishFlavor> dishFlavors = dishFlavorService.list(queryWrapper);
        dishDto.setFlavors(dishFlavors);
        return dishDto;
    }

    @Transactional
    public void updateWithFlavor(DishDto dishDto) {
        this.updateById(dishDto);

        Long dishId = dishDto.getId();
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, dishId);
        dishFlavorService.remove(queryWrapper);

        List<DishFlavor> dishFlavors = dishDto.getFlavors()
            .stream()
            .peek((item) -> item.setDishId(dishId))
            .collect(Collectors.toList());
        dishFlavorService.saveBatch(dishFlavors);
    }

}
