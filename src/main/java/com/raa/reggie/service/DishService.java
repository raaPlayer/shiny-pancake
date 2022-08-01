package com.raa.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.raa.reggie.dto.DishDto;
import com.raa.reggie.entity.Dish;

import java.util.List;

public interface DishService extends IService<Dish> {
    void saveWithFlavor(DishDto dishDto);

    DishDto getByIdWithFlavor(Long id);

    void updateWithFlavor(DishDto dishDto);
}
