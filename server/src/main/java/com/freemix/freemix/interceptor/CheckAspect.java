package com.freemix.freemix.interceptor;

import com.freemix.freemix.CheckToken;
import com.freemix.freemix.controller.BaseController;
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

        if ("dev".equals(environmentChecker.checkEnvironment())){
            log.info("不用检测token，本地测试环境");
            return joinPoint.proceed();
        }
        log.info("用检测token，线上环境");
        // 在生产环境中进行token验证
        Object token1 = redisTemplate.opsForValue().get("token");
        if(token1==null){
            return ApiResponse.failure("token过期");
        }
        String tokenRedis = (String) redisTemplate.opsForValue().get("token");
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        HttpServletRequest request = attributes.getRequest();

        // 获取Token（支持多种方式）
        String token = getTokenFromRequest(request);
        if(!token.equals(tokenRedis)){
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