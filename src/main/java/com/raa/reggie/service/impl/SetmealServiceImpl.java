package com.raa.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.raa.reggie.common.CustomException;
import com.raa.reggie.dto.SetmealDto;
import com.raa.reggie.entity.Setmeal;
import com.raa.reggie.entity.SetmealDish;
import com.raa.reggie.mapper.SetmealMapper;
import com.raa.reggie.service.SetmealDishService;
import com.raa.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;
    @Transactional
    public void saveWithDish(SetmealDto setmealDto) {
        this.save(setmealDto);
        Long id = setmealDto.getId();

        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes
            .stream()
            .peek((item) -> item.setSetmealId(id))
            .collect(Collectors.toList());

        setmealDishService.saveBatch(setmealDishes);
    }

    @Transactional
    public void removeWithDish(List<Long> ids) {
        //where id in (list) and status = 1;
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<Setmeal>()
            .in(Setmeal::getId, ids)
            .eq(Setmeal::getStatus, 1);
        //select count(*) from setmeal
        if(this.count(queryWrapper) > 0)
            throw new CustomException("套餐正在售卖中，不能删除。");

        this.removeByIds(ids);

        setmealDishService.remove(
            new LambdaQueryWrapper<SetmealDish>().in(SetmealDish::getSetmealId, ids)
        );
    }


}
