package com.freemix.freemix.interceptor;

import com.freemix.freemix.enetiy.ApiLog;
import com.freemix.freemix.enetiy.User;
import com.freemix.freemix.util.UserContextUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

@Aspect
@Component
@Slf4j
public class ApiLogAspect {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private UserContextUtil userContextUtil;

    // 定义切点：拦截 controller 包下的所有方法
    @Pointcut("execution(* com.freemix.freemix.controller..*.*(..))")
    public void logPointCut() {}

    @Around("logPointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        long beginTime = System.currentTimeMillis();
        
        // 执行目标方法
        Object result = point.proceed();
        
        // 执行时长
        long time = System.currentTimeMillis() - beginTime;
        
        // 异步保存日志
        saveLog(point, result, time);
        
        return result;
    }

    private void saveLog(ProceedingJoinPoint point, Object result, long time) {
        CompletableFuture.runAsync(() -> {
            try {
                // 获取请求上下文
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                HttpServletRequest request = attributes != null ? attributes.getRequest() : null;
                
                ApiLog apiLog = new ApiLog();
                apiLog.setSpendTime(time);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String formattedTime = LocalDateTime.now().format(formatter);
                apiLog.setCreateTimeStr(formattedTime);
                
                if (request != null) {
                    apiLog.setUrl(request.getRequestURL().toString());
                    apiLog.setMethod(request.getMethod());
                    apiLog.setIp(getIpAddress(request));
                    
                    // 获取当前用户信息
                    try {
                        User currentUser = userContextUtil.getCurrentUser();
                        if (currentUser != null) {
                            apiLog.setUsername(currentUser.getUsername());
                        }
                    } catch (Exception e) {
                        // 获取用户信息失败不影响日志记录
                        log.warn("获取用户信息失败", e);
                    }
                }
                
                // 获取类名和方法名
                String className = point.getTarget().getClass().getName();
                String methodName = point.getSignature().getName();
                apiLog.setClassMethod(className + "." + methodName);
                
                // 请求参数
                Object[] args = point.getArgs();
                // 简单的参数处理，实际项目中可能需要排除HttpServletRequest等无法序列化的参数
                // 这里暂不深入处理，MongoDB可以存储复杂对象
                apiLog.setArgs(args);
                
                // 返回结果
                apiLog.setResult(result);
                
                // 保存到 MongoDB
                mongoTemplate.save(apiLog);
                
            } catch (Exception e) {
                log.error("保存API日志失败", e);
            }
        });
    }
    
    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
