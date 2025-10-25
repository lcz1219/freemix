package com.freemix.freemix.controller;

import com.freemix.freemix.configurer.WebSocketUserManager;
import com.freemix.freemix.enetiy.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user-status")
public class UserStatusController {

    @Autowired
    private WebSocketUserManager webSocketUserManager;

    /**
     * 获取所有用户的在线状态
     * @return 用户在线状态映射
     */
    @GetMapping("/all")
    public Map<String, Boolean> getAllUserStatus() {
        Map<String, Boolean> userStatus = new HashMap<>();
        Map<String, Principal> onlineUsers = webSocketUserManager.getOnlineUsernames();
        List<String> onlineUsersList = onlineUsers.values().stream().map(Principal::getName).collect(Collectors.toList());
//        onlineUsersList.stream().map()
        // 将在线用户标记为true
        for (String username : onlineUsersList) {
            userStatus.put(username, true);
        }
        
        return userStatus;
    }

    /**
     * 检查特定用户是否在线
     * @param username 用户名
     * @return 是否在线
     */
    @GetMapping("/{username}")
    public boolean isUserOnline(@PathVariable String username) {
        return webSocketUserManager.isUserOnline(username);
    }

    /**
     * 获取在线用户数量
     * @return 在线用户数量
     */
    @GetMapping("/count")
    public int getOnlineUserCount() {
        return webSocketUserManager.getOnlineUserCount();
    }
}