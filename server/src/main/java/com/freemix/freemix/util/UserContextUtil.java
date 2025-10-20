package com.freemix.freemix.util;

import com.freemix.freemix.enetiy.AgentModel;
import com.freemix.freemix.enetiy.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 用户上下文工具类
 * 提供获取当前登录用户信息的功能
 */
@Component
public class UserContextUtil {

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 获取当前登录用户信息
     * @return User 当前用户对象，如果未登录则返回null
     */
    public User getCurrentUser() {
        // 获取当前请求
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }

        HttpServletRequest request = attributes.getRequest();
        
        // 从请求中获取token
        String token = getTokenFromRequest(request);
        String userAgent = request.getHeader("User-Agent");
        if(userAgent.contains(AgentModel.Electron)){
            if (token != null && !token.isEmpty()) {
                // 根据token查询用户信息
                Query query = new Query();
                query.addCriteria(Criteria.where("deskToken").is(token));
                return mongoTemplate.findOne(query, User.class);
            }
        }else{
            if (token != null && !token.isEmpty()) {
                // 根据token查询用户信息
                Query query = new Query();
                query.addCriteria(Criteria.where("token").is(token));
                return mongoTemplate.findOne(query, User.class);
            }
        }

        
        return null;
    }
    
    /**
     * 从请求中获取token
     * @param request HTTP请求
     * @return String token字符串
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        // 优先级1：Authorization头（标准方式）
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        String deskHeader = request.getHeader("X-Desktop-Token");
        if (deskHeader != null && !deskHeader.isEmpty()) {
            return deskHeader;
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