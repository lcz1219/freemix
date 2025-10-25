package com.freemix.freemix.configurer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;
import org.springframework.web.socket.WebSocketSession;

/**
 * WebSocket事件监听器
 */
@Component
public class WebSocketEventListener {

    @Autowired
    private WebSocketUserManager webSocketUserManager;

    /**
     * 处理WebSocket连接建立事件
     */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        String simSessionId = event.getMessage().getHeaders().get("simpSessionId", String.class);
        webSocketUserManager.userOnline(simSessionId,event.getUser());
        System.out.println("WebSocket连接已建立");
    }

    /**
     * 处理WebSocket断开连接事件
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        String simSessionId = event.getMessage().getHeaders().get("simpSessionId", String.class);
        webSocketUserManager.userOffline(simSessionId,event.getUser());
        System.out.println("WebSocket连接已断开");
    }

//    /**
//     * 处理用户订阅事件
//     */
//    @EventListener
//    public void handleWebSocketSubscribeListener(SessionSubscribeEvent event) {
//        WebSocketSession session = event.getMessage().getHeaders().get("simpSession", WebSocketSession.class);
//        if (session != null) {
//            webSocketUserManager.userOnline(session);
//        }
//        System.out.println("用户已订阅");
//    }
//
//    /**
//     * 处理用户取消订阅事件
//     */
//    @EventListener
//    public void handleWebSocketUnsubscribeListener(SessionUnsubscribeEvent event) {
//        System.out.println("用户已取消订阅");
//    }
}