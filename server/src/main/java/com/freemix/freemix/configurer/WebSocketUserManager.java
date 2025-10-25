package com.freemix.freemix.configurer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.security.Principal;
import java.util.Arrays;

/**
 * WebSocket用户在线状态管理器
 */
@Component
public class WebSocketUserManager {
    
    // 存储用户在线状态的映射表
    private final Map<String, Principal> onlineUsers = new ConcurrentHashMap<>();
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    /**
     * 用户上线
     * @param session WebSocket会话
     */
    public void userOnline(String sessionId, Principal principal) {

        
//        Principal principal = session.getPrincipal();
        if (principal != null) {
            String username = principal.getName();
            onlineUsers.put(sessionId, principal);
            System.out.println("用户上线: " + username + ", 当前在线用户数: " + onlineUsers.size());
            broadcastUserStatusUpdate();
        }
    }
    
    /**
     * 用户下线
     * @param session WebSocket会话
     */
    public void userOffline(String sessionId, Principal principal) {

        if (principal != null) {
            String username = principal.getName();
            Principal remove = onlineUsers.remove(sessionId);
            if (remove != null) {
                System.out.println("用户下线: " + username + ", 剩余在线用户数: " + onlineUsers.size());
                broadcastUserStatusUpdate();
            }
        }
    }
    
    /**
     * 检查用户是否在线
     * @param username 用户名
     * @return 是否在线
     */
    public boolean isUserOnline(String username) {
        return onlineUsers.containsKey(username);
    }
    
    /**
     * 获取在线用户数量
     * @return 在线用户数量
     */
    public int getOnlineUserCount() {
        return onlineUsers.size();
    }
    
    /**
     * 获取所有在线用户名
     * @return 在线用户名集合
     */
    public Map<String, Principal> getOnlineUsernames() {
       return  onlineUsers;
    }
    
    /**
     * 广播用户状态更新
     */
    private void broadcastUserStatusUpdate() {
        Map<String, Object> statusUpdate = new HashMap<>();
        statusUpdate.put("type", "user-status-update");
        statusUpdate.put("onlineUsers", Arrays.asList(getOnlineUsernames()));
        
        // 广播给所有连接的客户端
        messagingTemplate.convertAndSend("/topic/user-status", statusUpdate);
    }
}