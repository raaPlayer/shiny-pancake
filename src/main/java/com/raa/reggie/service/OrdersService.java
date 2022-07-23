package com.raa.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.raa.reggie.entity.Orders;

public interface OrdersService extends IService<Orders> {
    void submit(Orders orders);
}
