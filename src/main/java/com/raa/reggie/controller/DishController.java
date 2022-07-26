package com.raa.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.raa.reggie.common.BaseContext;
import com.raa.reggie.common.R;
import com.raa.reggie.dto.DishDto;
import com.raa.reggie.entity.Dish;
import com.raa.reggie.entity.DishFlavor;
import com.raa.reggie.service.CategoryService;
import com.raa.reggie.service.DishFlavorService;
import com.raa.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/dish")
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){
//        log.info("菜品数据" + dishDto.toString());
        dishService.saveWithFlavor(dishDto);
        //清理缓存，重点key，全清 dish.CategoryId=*
        String key = "dish.CategoryId=" + dishDto.getCategoryId();
        redisTemplate.delete(key);
        return R.success("添加菜品数据成功");
    }

    @GetMapping("/page")
    public R<Page<DishDto>> page(int page, int pageSize, String name){
        Page<Dish> pageInfo = new Page<>(page, pageSize);

        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotEmpty(name), Dish::getName, name)
                .eq(BaseContext.getUserRights() < 20 ,Dish::getStatus, 1)
                .orderByDesc(Dish::getUpdateTime);
        dishService.page(pageInfo, queryWrapper);

        Page<DishDto> dishDtoPage = new Page<>();
        //拷贝(源对象, 赋值对象, 不拷贝的属性)
        BeanUtils.copyProperties(pageInfo, dishDtoPage, "records");
//        List<Dish> records = ;
        dishDtoPage.setRecords(
            pageInfo.getRecords()
            .stream()
            .map((item) -> {
                DishDto dishDto = new DishDto();
                BeanUtils.copyProperties(item, dishDto);
                String s = categoryService
                        .getById(item.getCategoryId())
                        .getName();
                if(s != null)
                    dishDto.setCategoryName(s);
                return dishDto;
            })
            .collect(Collectors.toList())
        );
        return R.success(dishDtoPage);
    }

    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id){
        return R.success(dishService.getByIdWithFlavor(id));
    }

    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){
        dishService.updateWithFlavor(dishDto);
        String key = "dish.CategoryId=" + dishDto.getCategoryId();
        redisTemplate.delete(key);
        return R.success("更新菜品信息成功");
    }

    @GetMapping("list")
    public R<List<DishDto>> list(Dish dish){
        String dishes_key = "dish.CategoryId=" + dish.getCategoryId();
        List<DishDto> dishDtoLists = (List<DishDto>) redisTemplate.opsForValue().get(dishes_key);
        if(dishDtoLists != null){
            return R.success(dishDtoLists);
        }

        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<Dish>()
                .eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId())
                .eq(BaseContext.getUserRights() < 20, Dish::getStatus, 1)
                .orderByAsc(Dish::getSort)
                .orderByDesc(Dish::getUpdateTime);
        List<Dish> dishes = dishService.list(queryWrapper);

        dishDtoLists = dishes.stream()
                .map((item) -> {
                    DishDto dishDto = new DishDto();
                    BeanUtils.copyProperties(item, dishDto);
                    LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<DishFlavor>()
                            .eq(DishFlavor::getDishId, item.getId());
                    dishDto.setFlavors(dishFlavorService.list(lambdaQueryWrapper));
                    return dishDto;
                })
                .collect(Collectors.toList());
        redisTemplate.opsForValue().set(dishes_key, dishDtoLists, 1, TimeUnit.DAYS);
        return R.success(dishDtoLists);
    }

    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids){
        for(Long i : ids){
            Dish byId = dishService.getById(i);
            redisTemplate.delete("dish.CategoryId=" + byId.getCategoryId());
        }
        dishService.removeByIds(ids);
        return R.success("删除成功");
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
        for(Long id : ids){
            Dish byId = dishService.getById(id);
            redisTemplate.delete("dish.CategoryId=" + byId.getCategoryId());
        }
        LambdaUpdateWrapper<Dish> in = new LambdaUpdateWrapper<Dish>()
                .set(Dish::getStatus, i)
                .in(Dish::getId, ids);
        dishService.update(in);
        return R.success("成功");
    }
}
