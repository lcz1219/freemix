package com.freemix.freemix.configurer;

import com.freemix.freemix.enetiy.User;
import com.freemix.freemix.util.UserContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

@Component
public class WebSocketAuthenticationInterceptor implements HandshakeInterceptor {
    
    @Autowired
    private UserContextUtil userContextUtil;
    
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, 
                                  WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
            
            // 从HTTP请求中获取用户信息
            User user = userContextUtil.getCurrentUser();
            
            if (user != null) {
                // 创建Principal对象
                Principal principal = new Principal() {
                    @Override
                    public String getName() {
                        return user.getUsername();
                    }
                };
                
                // 将Principal存储在attributes中
                attributes.put("user", user);
                attributes.put("PRINCIPAL", principal);
                return true;
            }
        }
        // 如果没有认证信息，拒绝连接
        return false;
    }
    
    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, 
                              WebSocketHandler wsHandler, Exception exception) {
        // 握手完成后不需要特殊处理
    }
}