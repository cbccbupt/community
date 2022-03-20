package com.nowconder.community.config;

import com.nowconder.community.controller.intercepter.AlphaIntercepter;
import com.nowconder.community.controller.intercepter.LoginRequireIntercepter;
import com.nowconder.community.controller.intercepter.LoginTicketIntercepter;
import com.nowconder.community.controller.intercepter.MessageInterception;
import com.nowconder.community.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private AlphaIntercepter alphaIntercepter;

    @Autowired
    private LoginTicketIntercepter loginTicketIntercepter;

    @Autowired
    private LoginRequireIntercepter loginRequireIntercepter;

    @Autowired
    private MessageInterception messageInterception;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(alphaIntercepter)
                .excludePathPatterns("/**/*.css","/**/*.js","/**/*.png","/**/*.jpg","/**/*.jpeg")
                .addPathPatterns("/register","/login");

        registry.addInterceptor(loginTicketIntercepter)
                .excludePathPatterns("/**/*.css","/**/*.js","/**/*.png","/**/*.jpg","/**/*.jpeg");

        registry.addInterceptor(loginRequireIntercepter)
                .excludePathPatterns("/**/*.css","/**/*.js","/**/*.png","/**/*.jpg","/**/*.jpeg");

        registry.addInterceptor(messageInterception)
                .excludePathPatterns("/**/*.css","/**/*.js","/**/*.png","/**/*.jpg","/**/*.jpeg");

    }
}
