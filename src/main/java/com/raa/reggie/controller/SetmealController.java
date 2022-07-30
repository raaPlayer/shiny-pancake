package com.raa.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.raa.reggie.common.BaseContext;
import com.raa.reggie.common.R;
import com.raa.reggie.dto.SetmealDto;
import com.raa.reggie.entity.Setmeal;
import com.raa.reggie.service.CategoryService;
import com.raa.reggie.service.SetmealService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/setmeal")
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private CategoryService categoryService;

    @PostMapping
    @CacheEvict(value = "Setmeal.CategoryId", allEntries = true)    //清除val下所有key缓存
    public R<String> save(@RequestBody SetmealDto setmealDto){
        setmealService.saveWithDish(setmealDto);
        return R.success("新增套餐成功");
    }

    @GetMapping("/page")
    public R<Page<SetmealDto>> page(int page, int pageSize, String name){
        Page<Setmeal> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<Setmeal>()
                .like(StringUtils.isNotEmpty(name), Setmeal::getName, name)
                .eq(BaseContext.getUserRights() < 20, Setmeal::getStatus, 1)
                .orderByAsc(Setmeal::getUpdateTime);
        setmealService.page(pageInfo, queryWrapper);

        Page<SetmealDto> dtoPage = new Page<>();
        BeanUtils.copyProperties(pageInfo, dtoPage, "records");
        dtoPage.setRecords(
            pageInfo.getRecords()
            .stream()
            .map((item) -> {
                SetmealDto setmealDto = new SetmealDto();
                BeanUtils.copyProperties(item, setmealDto);
                String s = categoryService.getById(item.getCategoryId())
                        .getName();
                if(s != null)
                    setmealDto.setCategoryName(s);
                return setmealDto;
            })
            .collect(Collectors.toList())
        );

        return R.success(dtoPage);
    }

    @DeleteMapping
    @CacheEvict(value = "Setmeal.CategoryId", allEntries = true)
    public R<String> reomve(@RequestParam List<Long> ids){
        setmealService.removeWithDish(ids);
        return R.success("套餐数据删除成功");
    }
    @Cacheable(value = "Setmeal.CategoryId", key = "#setmeal.categoryId")
    @GetMapping("/list")
    public R<List<Setmeal>> list(Setmeal setmeal){
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<Setmeal>()
                .eq(Setmeal::getCategoryId, setmeal.getCategoryId())
                .eq(Setmeal::getStatus, 1)
                .orderByDesc(Setmeal::getUpdateTime);
        List<Setmeal> setmealList = setmealService.list(queryWrapper);
        return R.success(setmealList);
    }

    @PostMapping("/status/0")
    public R<String> statusUp(@RequestParam List<Long> ids){
        return setStatus(0, ids);
    }

    @PostMapping("/status/1")
    public R<String> statusDown(@RequestParam List<Long> ids){
        return setStatus(1, ids);
    }

    private R<String> setStatus(int i, List<Long> ids){
        LambdaUpdateWrapper<Setmeal> in = new LambdaUpdateWrapper<Setmeal>()
                .set(Setmeal::getStatus, i)
                .in(Setmeal::getId, ids);
        setmealService.update(in);
        return R.success("成功");
    }
}
