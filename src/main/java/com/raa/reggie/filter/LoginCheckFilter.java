package com.raa.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.raa.reggie.common.BaseContext;
import com.raa.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@WebFilter(filterName = "loginCheckFilter", urlPatterns = "/*")
public class LoginCheckFilter implements Filter {
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    private static final String[] OK_URLS = {
            "/employee/login",
            "/employee/logout",
            "/backend/**",
            "/front/**",
            "/user/sendMsg",
            "/user/login",
    };
//    "/common/**"

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
//        log.info("线程id：{}",Thread.currentThread().getId());

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String requestURI = request.getRequestURI();
        log.info("拦截到请求：{}",requestURI);

        for(String url: OK_URLS){
            if(PATH_MATCHER.match(url, requestURI)) {
                filterChain.doFilter(request, response);
                return;
            }
        }
        //一类账号登录后去移除其他类型账号session
        if(request.getSession().getAttribute("employee") != null){  //电脑员工登录
            BaseContext.setCurrentId((Long) request.getSession().getAttribute("employee"), 20L);
            filterChain.doFilter(request, response);
            return;
        }

        if(request.getSession().getAttribute("user") != null){  //手机消费者登录
            BaseContext.setCurrentId((Long) request.getSession().getAttribute("user"), 10L);
            filterChain.doFilter(request, response);
            return;
        }

        BaseContext.setCurrentId(0L, 0L);
        response.setContentType("text/json;charset=utf-8");
        response.getWriter().write(JSON.toJSONString(R.error("未登录")));
    }
}
