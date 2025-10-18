package com.freemix.freemix.interceptor;

import com.freemix.freemix.CheckToken;
import com.freemix.freemix.controller.BaseController;
import com.freemix.freemix.enetiy.AgentModel;
import com.freemix.freemix.enetiy.User;
import com.freemix.freemix.util.ApiResponse;
import com.freemix.freemix.util.EnvironmentChecker;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Aspect

@Slf4j
@Component
public class CheckAspect extends BaseController  {

    private final RedisTemplate redisTemplate;
    private final EnvironmentChecker environmentChecker;
    @Autowired
    private MongoTemplate mongoTemplate;

    // 使用构造函数注入 + @Lazy 解决循环依赖
    public CheckAspect(@Lazy RedisTemplate redisTemplate, EnvironmentChecker environmentChecker) {
        this.redisTemplate = redisTemplate;
        this.environmentChecker = environmentChecker;
    }

    @Pointcut("@annotation(com.freemix.freemix.CheckToken)")
    public void pointcut() {

    }

    @Around("pointcut()")
    public Object check(ProceedingJoinPoint joinPoint) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        String userAgent = request.getHeader("User-Agent");
        if ("dev".equals(environmentChecker.checkEnvironment())){
            log.info("不用检测token，本地测试环境");
            return joinPoint.proceed();
        }
        log.info("用检测token，线上环境");


        // 检查是否为桌面端请求（通过X-Desktop-Token头）
        String desktopToken = request.getHeader("X-Desktop-Token");
        if (desktopToken != null && !desktopToken.isEmpty()) {
            // 处理桌面端token
            return handleDesktopToken(request, joinPoint, desktopToken);
        }

        // 获取Token（支持多种方式）
        String token = getTokenFromRequest(request);
        if(token == null || token.isEmpty()){
            log.info("token不能为空");
            return ApiResponse.failure("token不能为空");
        }

        // 根据token从数据库获取用户信息
        User user = getUserByToken(token);
        if(user == null){
            log.info("token无效");
            return ApiResponse.failure("token无效");
        }

        // 使用用户名_token作为Redis key进行验证（Web端token）
        String tokenKey = user.getUsername() + "_token";
        String tokenRedis = (String) redisTemplate.opsForValue().get(tokenKey);
        if(tokenRedis == null){
            log.info("token过期");
            return ApiResponse.failure("token过期");
        }
        
        if(!token.equals(tokenRedis)){
            log.info("token不正确");
            return ApiResponse.failure("token不正确");
        }

        // Token有效，继续执行业务逻辑
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
    
    /**
     * 处理桌面端token验证
     * @param request HTTP请求
     * @param joinPoint 切入点
     * @param desktopToken 桌面端token
     * @return Object 处理结果
     */
    private Object handleDesktopToken(HttpServletRequest request, ProceedingJoinPoint joinPoint, String desktopToken) throws Throwable {
        // 使用desktop_token_作为Redis key前缀
        String desktopTokenKey = "desktop_token_" + desktopToken;
        
        // 从Redis中获取桌面端token信息
        String userId = (String) redisTemplate.opsForValue().get(desktopTokenKey);
        if (userId == null) {
            log.info("桌面端token无效或已过期");
            return ApiResponse.failure("桌面端token无效或已过期");
        }
        
        // 延长桌面端token有效期（30天）
        redisTemplate.expire(desktopTokenKey, 30, TimeUnit.DAYS);
        
        // 继续执行业务逻辑
        return joinPoint.proceed();
    }
    
    /**
     * 根据token获取用户信息
     * @param token 用户token
     * @return User 用户对象
     */
    public User getUserByToken(String token) {
        Query query = new Query();
        query.addCriteria(Criteria.where("token").is(token));
        return mongoTemplate.findOne(query, User.class);
    }


}