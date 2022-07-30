package com.raa.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.raa.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@WebFilter(filterName = "Level_20_Request", urlPatterns = {"/category/*","/dish/*","/setmeal/*","/common/*","/employee/*"})
public class LoginCheckFilter1 implements Filter {
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    private static final String[] OK_URLS = {
            "/employee/login",
            "/employee/logout",
    };

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String requestURI = request.getRequestURI();
        for(String url: OK_URLS){
            if(PATH_MATCHER.match(url, requestURI)) {
                filterChain.doFilter(request, response);
                return;
            }
        }
        String method = request.getMethod();
        log.info("请求方式:{} 拦截到请求：{}", method, requestURI);
        if(method.equals("GET") && (request.getSession().getAttribute("employee") != null || request.getSession().getAttribute("user") != null)) {
//            log.info("是GET请求");   //有登录就可放行
            filterChain.doFilter(request, response);
            return;
        }
        else if(request.getSession().getAttribute("employee") != null) {
//            log.info("get以外请求，需employee登录");即增删改
            filterChain.doFilter(request, response);
            return;
        }
        response.setContentType("text/json;charset=utf-8");
        response.getWriter().write(JSON.toJSONString(R.error("未登录")));
    }
}
