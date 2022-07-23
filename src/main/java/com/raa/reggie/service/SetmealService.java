package com.raa.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.raa.reggie.dto.SetmealDto;
import com.raa.reggie.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {
    void saveWithDish(SetmealDto setmealDto);

    void removeWithDish(List<Long> ids);
}
