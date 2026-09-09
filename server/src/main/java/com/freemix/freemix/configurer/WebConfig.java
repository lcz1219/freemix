package com.freemix.freemix.configurer;


import com.freemix.freemix.interceptor.LogInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        // AI 流式对话等异步请求耗时可能超过 Tomcat 默认的 30 秒超时，
        // 这里放大到 10 分钟，避免流式响应被容器中途强制断开
        configurer.setDefaultTimeout(10 * 60 * 1000L);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册拦截器到Spring MVC机制，同时可以指定拦截匹配路径和排除路径
        registry.addInterceptor(new LogInterceptor())
                .addPathPatterns("/**") // 指定拦截匹配的路径
                .excludePathPatterns("/login", "/error"); // 指定不拦截的路径
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 获取项目根目录下的file文件夹路径
        String filePath = System.getProperty("user.dir") + File.separator + "file" + File.separator;
        // 映射/file/**路径到服务器本地的file文件夹
        registry.addResourceHandler("/file/**")
                .addResourceLocations("file:" + filePath);
    }
}