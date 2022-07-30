package com.raa.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.raa.reggie.common.BaseContext;
import com.raa.reggie.common.R;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@WebFilter(filterName = "Level_15_Request", urlPatterns = {"/addressBook/*","/shoppingCart/*","/setmeal/*", "/order/submit"})
public class LoginCheckFilter2 implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        log.info("拦截到请求：{}",request.getRequestURI());
        if(request.getSession().getAttribute("user") != null){  //手机消费者登录
            filterChain.doFilter(request, response);
            return;//员工账号凭id通过了部分验证，终归无法下单。可考虑统一账号类型，而由权限进行区分
        }
        response.setContentType("text/json;charset=utf-8");
        response.getWriter().write(JSON.toJSONString(R.error("未登录")));
    }
}
