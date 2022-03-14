package com.nowconder.community.controller.intercepter;

import com.nowconder.community.annotation.LoginRequired;
import com.nowconder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

@Component
public class LoginRequireIntercepter implements HandlerInterceptor {

    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 判断拦截目标handler是否是方法
        if(handler instanceof HandlerMethod){
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Method method = handlerMethod.getMethod(); // 获取方法
            LoginRequired loginRequired = method.getAnnotation(LoginRequired.class); // 获取注解，判断注解是否为LoginRequired
            if(loginRequired != null && hostHolder.getUser() == null) {
                // 注解非空则包含loginrequired注解，且未登录时，拦截并跳转登录页面
                response.sendRedirect(request.getContextPath() + "/login");
                return false;
            }
        }
        return true;
    }
}
