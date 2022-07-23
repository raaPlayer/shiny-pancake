package com.raa.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.raa.reggie.entity.OrderDetail;
import com.raa.reggie.mapper.OrderDetailMappers;
import com.raa.reggie.service.OrderDetailService;
import org.springframework.stereotype.Service;

@Service
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailMappers, OrderDetail> implements OrderDetailService {
}
