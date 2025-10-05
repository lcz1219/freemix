package com.freemix.freemix.controller;

import com.alibaba.fastjson2.JSONObject;
import com.freemix.freemix.CheckToken;
import com.freemix.freemix.enetiy.Message;
import com.freemix.freemix.service.MessageService;
import com.freemix.freemix.util.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 消息控制器
 * 处理用户间的消息通信功能
 */
@RestController
@RequestMapping("/messages")
@CrossOrigin(origins = "*") // 允许跨域请求
public class MessageController {
    
    @Autowired
    private MessageService messageService;
    
    /**
     * 发送消息
     * @param message 消息对象
     * @return ApiResponse 包含发送结果的响应
     */
    @PostMapping("/send")
    @CheckToken
    public ApiResponse<Message> sendMessage(@RequestBody Message message) {
        return messageService.sendMessage(message);
    }
    
    /**
     * 获取当前用户收到的消息列表
     * @return ApiResponse 包含消息列表的响应
     */
    @GetMapping("/received")
    @CheckToken
    public ApiResponse<List<Message>> getReceivedMessages() {
        return messageService.getReceivedMessages();
    }
    
    /**
     * 获取当前用户发送的消息列表
     * @return ApiResponse 包含消息列表的响应
     */
    @GetMapping("/sent")
    @CheckToken
    public ApiResponse<List<Message>> getSentMessages() {
        return messageService.getSentMessages();
    }
    
    /**
     * 标记消息为已读
     * @param messageId 消息ID
     * @return ApiResponse 包含操作结果的响应
     */
    @PostMapping("/markAsRead/{messageId}")
    @CheckToken
    public ApiResponse<Message> markAsRead(@PathVariable String messageId) {
        return messageService.markAsRead(messageId);
    }
    
    /**
     * 批量标记消息为已读
     * @param messageIds 消息ID列表
     * @return ApiResponse
     */
    @PostMapping("/markAsReadBatch")
    @CheckToken
    public ApiResponse<String> markAsReadBatch(@RequestBody String body) {
        List<String> messageIds= JSONObject.parseObject(body).getJSONArray("messageIds").toJavaList(String.class);
        return messageService.markAsReadBatch(messageIds);
    }
    
    /**
     * 获取与指定用户的聊天记录
     * @param username 用户名
     * @return ApiResponse
     */
    @GetMapping("/chat/{username}")
    @CheckToken
    public ApiResponse<List<Message>> getChatHistory(@PathVariable String username) {
        return messageService.getChatHistory(username);
    }
    
    /**
     * 获取未读消息数量
     * @return ApiResponse 包含未读消息数量的响应
     */
    @GetMapping("/unreadCount")
    @CheckToken
    public ApiResponse<Long> getUnreadCount() {
        return messageService.getUnreadCount();
    }
    
    /**
     * 删除消息
     * @param messageId 消息ID
     * @return ApiResponse
     */
    @DeleteMapping("/delete/{messageId}")
    @CheckToken
    public ApiResponse<String> deleteMessage(@PathVariable String messageId) {
        return messageService.deleteMessage(messageId);
    }
}