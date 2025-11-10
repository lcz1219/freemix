package com.freemix.freemix.controller;

import com.alibaba.fastjson2.JSONObject;
import com.freemix.freemix.CheckToken;
import com.freemix.freemix.enetiy.AIMessage;
import com.freemix.freemix.service.AIMessageService;
import com.freemix.freemix.util.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AI消息控制器
 * 处理AI助手对话记录的保存和查询
 */
@RestController
@RequestMapping("/ai-messages")
@CrossOrigin(origins = "*") // 允许跨域请求
public class AIMessageController {

    private static final Logger log = LoggerFactory.getLogger(AIMessageController.class);
    
    @Autowired
    private AIMessageService aiMessageService;
    
    /**
     * 保存AI消息
     * @param aiMessage AI消息对象
     * @return ApiResponse 包含保存结果的响应
     */
    @PostMapping("/save")
    @CheckToken
    public ApiResponse<AIMessage> saveAIMessage(@RequestBody String body) {
        AIMessage aiMessage = JSONObject.parseObject(body, AIMessage.class);
        log.info("保存AI消息: 用户问题 = {}", aiMessage.getUserQuestion());
        return aiMessageService.saveAIMessage(aiMessage);
    }
    
    /**
     * 获取当前用户的所有AI消息记录
     * @return ApiResponse 包含消息列表的响应
     */
    @GetMapping("/all")
    @CheckToken
    public ApiResponse<List<AIMessage>> getAllAIMessages() {
        log.info("获取用户所有AI消息记录");
        return aiMessageService.getAllAIMessages();
    }
    
    /**
     * 获取当前用户的最近N条AI消息记录
     * @param limit 限制条数
     * @return ApiResponse 包含消息列表的响应
     */
    @GetMapping("/recent")
    @CheckToken
    public ApiResponse<List<AIMessage>> getRecentAIMessages(@RequestParam(defaultValue = "50") int limit) {
        log.info("获取用户最近 {} 条AI消息记录", limit);
        return aiMessageService.getRecentAIMessages(limit);
    }
    
    /**
     * 获取当前用户在指定日期范围的AI消息记录
     * @param startTime 开始时间戳（毫秒）
     * @param endTime 结束时间戳（毫秒）
     * @return ApiResponse 包含消息列表的响应
     */
    @GetMapping("/by-date-range")
    @CheckToken
    public ApiResponse<List<AIMessage>> getAIMessagesByDateRange(
            @RequestParam long startTime,
            @RequestParam long endTime) {
        log.info("获取用户在指定时间范围的AI消息记录: {} - {}", startTime, endTime);
        return aiMessageService.getAIMessagesByDateRange(startTime, endTime);
    }
    
    /**
     * 删除指定的AI消息记录
     * @param messageId 消息ID
     * @return ApiResponse 包含操作结果的响应
     */
    @DeleteMapping("/delete/{messageId}")
    @CheckToken
    public ApiResponse<String> deleteAIMessage(@PathVariable String messageId) {
        log.info("删除AI消息记录: {}", messageId);
        return aiMessageService.deleteAIMessage(messageId);
    }
    
    /**
     * 清空当前用户的所有AI消息记录
     * @return ApiResponse 包含操作结果的响应
     */
    @DeleteMapping("/clear-all")
    @CheckToken
    public ApiResponse<String> clearAllAIMessages() {
        log.info("清空用户所有AI消息记录");
        return aiMessageService.clearAllAIMessages();
    }
    
    /**
     * 获取当前用户的AI消息总数
     * @return ApiResponse 包含消息总数的响应
     */
    @GetMapping("/count")
    @CheckToken
    public ApiResponse<Long> getAIMessageCount() {
        log.info("获取用户AI消息总数");
        return aiMessageService.getAIMessageCount();
    }
    
    /**
     * 获取指定用户的历史AI消息记录
     * @param username 用户名
     * @return ApiResponse 包含消息列表的响应
     */
    @GetMapping("/{username}/history")
    @CheckToken
    public ApiResponse<List<AIMessage>> getUserHistory(@PathVariable String username) {
        log.info("获取用户 {} 的历史AI消息记录", username);
        return aiMessageService.getUserHistory(username);
    }
}