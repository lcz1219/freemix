package com.freemix.freemix.controller;

import com.alibaba.fastjson2.JSONObject;
import com.freemix.freemix.CheckToken;
import com.freemix.freemix.service.FriendshipService;
import com.freemix.freemix.util.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 好友系统控制器
 * 处理好友申请、通过、拒绝、删除、搜索等操作
 */
@RestController
@RequestMapping("/friend")
public class FriendshipController {

    @Autowired
    private FriendshipService friendshipService;

    /**
     * 发送好友申请
     */
    @PostMapping("/request")
    @CheckToken
    public ApiResponse sendRequest(@RequestBody String body) {
        JSONObject json = JSONObject.parseObject(body);
        String toUser = json.getString("toUser");
        return friendshipService.sendRequest(toUser);
    }

    /**
     * 通过好友申请
     */
    @PostMapping("/accept")
    @CheckToken
    public ApiResponse acceptRequest(@RequestBody String body) {
        JSONObject json = JSONObject.parseObject(body);
        String requestId = json.getString("requestId");
        return friendshipService.acceptRequest(requestId);
    }

    /**
     * 拒绝好友申请
     */
    @PostMapping("/reject")
    @CheckToken
    public ApiResponse rejectRequest(@RequestBody String body) {
        JSONObject json = JSONObject.parseObject(body);
        String requestId = json.getString("requestId");
        return friendshipService.rejectRequest(requestId);
    }

    /**
     * 获取我的好友列表
     */
    @GetMapping("/list")
    @CheckToken
    public ApiResponse getFriendList() {
        return friendshipService.getFriendList();
    }

    /**
     * 获取待处理的好友申请
     */
    @GetMapping("/pending")
    @CheckToken
    public ApiResponse getPendingRequests() {
        return friendshipService.getPendingRequests();
    }

    /**
     * 获取待处理申请数量（用于红点/徽标）
     */
    @GetMapping("/pending/count")
    @CheckToken
    public ApiResponse getPendingCount() {
        return friendshipService.getPendingCount();
    }

    /**
     * 删除好友
     */
    @PostMapping("/delete")
    @CheckToken
    public ApiResponse deleteFriend(@RequestBody String body) {
        JSONObject json = JSONObject.parseObject(body);
        String friendUsername = json.getString("friendUsername");
        return friendshipService.deleteFriend(friendUsername);
    }

    /**
     * 搜索用户（用于添加好友）
     */
    @GetMapping("/search")
    @CheckToken
    public ApiResponse searchUser(@RequestParam String keyword) {
        return friendshipService.searchUser(keyword);
    }
}
