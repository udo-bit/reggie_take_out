package com.itheima.reggie.filter;

import com.alibaba.fastjson2.JSON;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.AntPathMatcher;

import java.io.IOException;


@WebFilter(filterName = "LoginCheckFilter", urlPatterns = "/*")
public class LoginCheckFilter implements Filter {


    private static final AntPathMatcher matcher = new AntPathMatcher();


    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        // 1. 对请求和相应进行处理
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        // 2. 获取当前的访问地址
        String url = request.getRequestURI();
        // 3. 判断当前访问地址是否需要登录

        String[] needNotLogin = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/common/**",
                "/user/sendMsg",
                "/user/login"
        };


        boolean justGo = check(needNotLogin, url);
        if (justGo) {
            // 4. 如果不需要登录，直接放行
            filterChain.doFilter(request, response);
            return;
        }
        if (request.getSession().getAttribute("employee") != null) {
            Long empId = (Long) request.getSession().getAttribute("employee");
            BaseContext.setCurrentId(empId);
            // 5. 如果已经登录，直接放行
            filterChain.doFilter(request, response);
            return;
        }

        if (request.getSession().getAttribute("user") != null) {
            Long userId = (Long) request.getSession().getAttribute("user");
            BaseContext.setCurrentId(userId);
            filterChain.doFilter(request, response);
            return;
        }
        
        response.getWriter().write(JSON.toJSONString(R.error("请先登录")));
        return;
    }

    private boolean check(String[] urls, String requestURI) {
        for (String url : urls) {
            boolean match = matcher.match(url, requestURI);
            if (match) {
                return true;
            }
        }
        return false;
    }
}
