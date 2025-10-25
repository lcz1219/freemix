package com.freemix.freemix.controller;

import com.freemix.freemix.configurer.WebSocketUserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Controller
public class WebSocketMessageController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private WebSocketUserManager webSocketUserManager;

    /**
     * 处理用户状态更新消息
     * @param message 消息内容
     * @param principal 当前用户
     */
    @MessageMapping("/user-status")
    public void handleUserStatusUpdate(@Payload String message, Principal principal) {
        // 广播用户在线状态更新
        Map<String, Object> statusUpdate = new HashMap<>();
        statusUpdate.put("type", "user-status-update");
        statusUpdate.put("onlineUsers", Arrays.asList(webSocketUserManager.getOnlineUsernames()));
        
        // 广播给所有连接的客户端
        messagingTemplate.convertAndSend("/topic/user-status", statusUpdate);
    }
}