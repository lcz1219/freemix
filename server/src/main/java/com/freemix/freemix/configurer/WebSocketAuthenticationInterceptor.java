package com.freemix.freemix.configurer;

import com.freemix.freemix.enetiy.AgentModel;
import com.freemix.freemix.enetiy.User;
import com.freemix.freemix.util.UserContextUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;


import java.security.Principal;
import java.util.Map;

@Slf4j
@Component
public class WebSocketAuthenticationInterceptor implements HandshakeInterceptor {
    
    @Autowired
    private MongoTemplate mongoTemplate;
    
//    @Autowired
//    private WebSocketUserManager webSocketUserManager;
    
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                  WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
            HttpServletRequest httpRequest = servletRequest.getServletRequest();
            
            // 从URL参数或请求头中获取token
            String deskToken = httpRequest.getParameter("deskToken");
            String token = httpRequest.getParameter("token");
            
            // 如果URL参数中没有，则尝试从请求头获取
            if ((deskToken == null || deskToken.isEmpty()) && (token == null || token.isEmpty())) {
                String userAgent = httpRequest.getHeader("User-Agent");
                if (userAgent != null && userAgent.contains(AgentModel.Electron)) {
                    deskToken = httpRequest.getHeader("X-Desktop-Token");
                } else {
                    String authHeader = httpRequest.getHeader("Authorization");
                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
                        token = authHeader.substring(7);
                    }
                }
            }
            
            User user = null;
            
            // 根据不同的token类型查询用户
            if (deskToken != null && !deskToken.isEmpty()) {
                // 根据deskToken查询用户信息（桌面端）
                Query query = new Query();
                query.addCriteria(Criteria.where("deskToken").is(deskToken));
                user = mongoTemplate.findOne(query, User.class);
            } else if (token != null && !token.isEmpty()) {
                // 根据token查询用户信息（移动端）
                Query query = new Query();
                query.addCriteria(Criteria.where("token").is(token));
                user = mongoTemplate.findOne(query, User.class);
            }
            log.info("WebSocketAuthenticationInterceptor,user------>{}", user);
            final User finalUser=user;
            
            if (finalUser != null) {
                // 创建Principal对象
                Principal principal = new Principal() {
                    @Override
                    public String getName() {
                        return finalUser.getUsername();
                    }
                };
                
                // 将Principal存储在attributes中
                attributes.put("user", user);
                attributes.put("PRINCIPAL", principal);
                return true;
            }
            
            // 认证失败时先用 sendError 提交 401 响应
            // 这样 SockJS 后续的 sendError(503) 会因为响应已提交而失败
            // 客户端收到 401 后识别为认证失败，不会像收到 503 那样持续重试
            if (response instanceof ServletServerHttpResponse) {
                HttpServletResponse rawResponse = ((ServletServerHttpResponse) response).getServletResponse();
                if (!rawResponse.isCommitted()) {
                    rawResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "未授权，请提供有效的token");
                }
            }
        }
        // 如果没有认证信息，拒绝连接
        return false;
    }
    
    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, 
                              WebSocketHandler wsHandler, Exception exception) {
        // 握手完成后不需要特殊处理
        log.info("WebSocket handshake completed");
    }
}