package com.raa.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.raa.reggie.common.BaseContext;
import com.raa.reggie.common.CustomException;
import com.raa.reggie.entity.*;
import com.raa.reggie.mapper.OrdersMapper;
import com.raa.reggie.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrdersService {

    @Autowired
    private ShoppingCartService shoppingService;

    @Autowired
    private AddressBookService addressService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderDetailService orderDetailService;

    @Transactional
    public void submit(Orders orders) {
        Long userId = BaseContext.getCurrentId();
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<ShoppingCart>()
                .eq(ShoppingCart::getUserId, userId);
        List<ShoppingCart> shoppingList = shoppingService.list(wrapper);
        if(shoppingList == null || shoppingList.size() == 0)
            throw new CustomException("购物车为空！下单失败");
        AddressBook addressBook = addressService.getById(orders.getAddressBookId());
        if(addressBook == null)
            throw new CustomException("地址为空，无法派送！下单失败");

        User user = userService.getById(userId);
        long orderId = IdWorker.getId();    //生成订单号
        orders.setNumber(String.valueOf(orderId));
        orders.setUserId(userId);
        orders.setUserName(user.getName());
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());
        orders.setOrderTime(LocalDateTime.now());
        orders.setSumNum(shoppingList.size());
        //未做真正的支付功能，假数据
        orders.setStatus(2);
        orders.setCheckoutTime(LocalDateTime.now());
        //有关多线程计算可能出错
        AtomicInteger amount = new AtomicInteger(0);
        List<OrderDetail> orderDetails = shoppingList.stream()
                .map((item) -> {
                    OrderDetail detail = new OrderDetail();
                    detail.setName(item.getName());
                    detail.setOrderId(orderId);
                    detail.setDishId(item.getDishId());
                    detail.setSetmealId(item.getSetmealId());
                    detail.setDishFlavor(item.getDishFlavor());
                    detail.setNumber(item.getNumber());
                    detail.setAmount(item.getAmount());
                    detail.setImage(item.getImage());
                    amount.addAndGet(item.getAmount().multiply(new BigDecimal(item.getNumber())).intValue());
                    return detail;
                })
                .collect(Collectors.toList());
        orders.setAmount(new BigDecimal(amount.get()));
        this.save(orders);
        orderDetailService.saveBatch(orderDetails);
        shoppingService.remove(wrapper);
    }
}
