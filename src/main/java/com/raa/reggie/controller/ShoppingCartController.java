package com.raa.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.raa.reggie.common.BaseContext;
import com.raa.reggie.common.R;
import com.raa.reggie.entity.ShoppingCart;
import com.raa.reggie.service.ShoppingCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/shoppingCart")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shopService;

    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart){
        Long UserId = BaseContext.getCurrentId();
        shoppingCart.setUserId(UserId);

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<ShoppingCart>()
                .eq(ShoppingCart::getUserId, UserId);
        Long dishId = shoppingCart.getDishId();
        if(dishId != null)
            queryWrapper.eq(ShoppingCart::getDishId, dishId);
        else
            queryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());

        ShoppingCart shoppingCartOne = shopService.getOne(queryWrapper);
        if(shoppingCartOne != null){
            shoppingCartOne.setNumber(shoppingCartOne.getNumber() + 1);
            shopService.updateById(shoppingCartOne);
        }
        else {
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shopService.save(shoppingCart);
            shoppingCartOne = shoppingCart;
        }

        return R.success(shoppingCartOne);
    }

    @GetMapping("/list")
    public R<List<ShoppingCart>> list(){
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<ShoppingCart>()
                .eq(ShoppingCart::getUserId,BaseContext.getCurrentId())
                .orderByAsc(ShoppingCart::getCreateTime);

        List<ShoppingCart> shoppingCartList = shopService.list(queryWrapper);
        return R.success(shoppingCartList);
    }

    @DeleteMapping("/clean")
    public R<String> clean(){
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<ShoppingCart>()
                .eq(ShoppingCart::getUserId, BaseContext.getCurrentId());
        shopService.remove(queryWrapper);
        return R.success("清空购物车成功");
    }
}
