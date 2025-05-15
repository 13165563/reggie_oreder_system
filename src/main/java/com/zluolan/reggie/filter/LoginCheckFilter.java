package com.zluolan.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.zluolan.reggie.common.BaseContext;
import com.zluolan.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 检查用户是否已经完成登录
 */
// 过滤器注解，表示过滤器名称和过滤器路径
@WebFilter(filterName = "loginCheckFilter", urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {
    //路径匹配器，支持通配符匹配
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        //1、获取本次请求的URI
        String requestURI = request.getRequestURI();// /backend/index.html

        log.info("拦截到请求：{}", requestURI);

        //定义不需要处理的请求路径，登录，退出，静态资源
        String[] urls = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/common/**",
                "/user/sendMsg", // 新增发送短信的路径
                "/user/login", // 新增用户登录的路径
                "doc.html",
                "/webjars/**",
                "/swagger-resources",
                "/v2/api-docs"
        };


        //2、判断本次请求是否需要处理
        boolean check = check(urls, requestURI);

        //3、如果不需要处理，则直接放行
        if (check) {
            log.info("本次请求{}不需要处理", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        //4、判断登录状态，如果已登录过，且携带正确session，则直接放行
        //获取session中保存的当前登录用户id
        if (request.getSession().getAttribute("employee") != null) {
            log.info("用户已登录，用户id为：{}", request.getSession().getAttribute("employee"));
//            long id = Thread.currentThread().getId();
//            log.info("当前线程id为：{}", id);
            Long empId = (Long) request.getSession().getAttribute("employee"); // 获取session中保存的当前登录用户id
            BaseContext.setCurrentId(empId); // 将当前登录用户id保存到ThreadLocal中

            filterChain.doFilter(request, response);
            return;
        }

        // 判断移动端登录状态，注意这里也要去前端front/js/request.js中修改未登录为 'NOTLOGIN'
        if (request.getSession().getAttribute("user") != null) {
            log.info("用户已登录，用户id为：{}", request.getSession().getAttribute("user"));
//            long id = Thread.currentThread().getId();
//            log.info("当前线程id为：{}", id);
            Long userId = (Long) request.getSession().getAttribute("user"); // 获取session中保存的当前登录用户id
            BaseContext.setCurrentId(userId); // 将当前登录用户id保存到ThreadLocal中
            filterChain.doFilter(request, response);
            return;

        }


        log.info("用户未登录");
        //5、如果未登录则返回未登录结果，通过输出流方式向客户端页面响应数据，前端拦截响应，相应跳转到登录页面
        // 响应数据为json格式
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        log.info("拦截，交由前端跳转");

    }

    /**
     * 路径匹配，检查本次请求是否需要放行
     *
     * @param urls
     * @param requestURI
     * @return
     */
    public boolean check(String[] urls, String requestURI) {
        for (String url : urls) {
            // match方法用于判断两个路径是否匹配tch：匹配
            boolean match = PATH_MATCHER.match(url, requestURI);
            if (match) {
                return true;
            }
        }
        return false;
    }

}
