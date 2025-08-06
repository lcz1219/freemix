package com.freemix.freemix.interceptor;

import com.freemix.freemix.CheckToken;
import com.freemix.freemix.util.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect

@Slf4j
@Component
public class CheckAspect  {

    private final RedisTemplate redisTemplate;

    // 使用构造函数注入 + @Lazy 解决循环依赖
    public CheckAspect(@Lazy RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Pointcut("@annotation(com.freemix.freemix.CheckToken)")
    public void pointcut() {

    }

    @Around("pointcut()")
    public Object check(ProceedingJoinPoint joinPoint) throws Throwable {
        Object token1 = redisTemplate.opsForValue().get("token");
        if(token1==null){
            return ApiResponse.failure("token过期");
        }
        String tokenRedis = (String) redisTemplate.opsForValue().get("token");
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        HttpServletRequest request = attributes.getRequest();

        // 2. 获取Token（支持多种方式）
        String token = getTokenFromRequest(request);
        if(!token.equals(tokenRedis)){
            return ApiResponse.failure("token不正确");
        }

        // 4. Token有效，继续执行业务逻辑
        return joinPoint.proceed();

    }
    private String getTokenFromRequest(HttpServletRequest request) {
        // 优先级1：Authorization头（标准方式）
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        // 优先级2：自定义Header
        String customHeader = request.getHeader("X-Auth-Token");
        if (customHeader != null && !customHeader.isEmpty()) {
            return customHeader;
        }

        // 优先级3：URL参数（备用方案）
        return request.getParameter("token");
    }


}