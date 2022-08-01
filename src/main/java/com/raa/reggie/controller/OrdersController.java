package com.raa.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.raa.reggie.common.BaseContext;
import com.raa.reggie.common.R;
import com.raa.reggie.entity.Orders;
import com.raa.reggie.service.OrdersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/order")
public class OrdersController {

    @Autowired
    private OrdersService ordersService;

    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders){
        ordersService.submit(orders);
        return R.success("下单成功");
    }

    @GetMapping("/page")
    public R<Page<Orders>> page(int page, int pageSize, Long number, @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime beginTime, @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime){
//        log.info("分页参数 page: {}, pageSize: {}, number: {}, begin: {}, end{}", page, pageSize, number, beginTime, endTime);
        Page<Orders> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<Orders>()
                .eq(number != null, Orders::getNumber, number)
                .gt(beginTime != null, Orders::getOrderTime, beginTime)
                .lt(endTime != null, Orders::getOrderTime, endTime);
        ordersService.page(pageInfo, wrapper);
        return R.success(pageInfo);
    }

    @PutMapping
    public R<String> update(@RequestBody Orders orders){
//        log.info("更新接收参数：{}", orders);
        ordersService.updateById(orders);
        return R.success("更改订单状态成功");
    }

    @GetMapping("/userPage")
    public R<Page<Orders>> userPage(int page, int pageSize){
        log.info("用户展示订单，参数: page: {}, pageSize: {}", page, pageSize);
        Page<Orders> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<Orders>()
                .eq(Orders::getUserId, BaseContext.getCurrentId());
        ordersService.page(pageInfo, wrapper);
        return R.success(pageInfo);
    }

    @PostMapping("/again")
    public R<String> again(@RequestBody Long id){
        log.info("再来一单的orders.id: {} \n也许是希望通过id重新查询将商品加入购物车，前端只需code=1，就跳转到index。未实现", id);

        return R.success("成功");
    }
}
