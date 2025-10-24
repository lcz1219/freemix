package com.freemix.freemix.configurer;

import ch.qos.logback.classic.pattern.MessageConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.List;

@Configuration
@EnableWebSocketMessageBroker // 启用基于代理的WebSocket消息处理
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Autowired
    private WebSocketAuthenticationInterceptor webSocketAuthenticationInterceptor;
    
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 客户端将连接到此端点，使用SockJS作为备选方案
        registry.addEndpoint("/ws")
                .addInterceptors(webSocketAuthenticationInterceptor)
                .setHandshakeHandler(new CustomHandshakeHandler())
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }


    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 启用一个简单的内存消息代理，客户端订阅以接收消息
        registry.enableSimpleBroker("/topic", "/user");
        // 客户端发送消息的目的地前缀
        registry.setApplicationDestinationPrefixes("/app");
        // 设置用户目的地前缀
        registry.setUserDestinationPrefix("/user");
    }

}
