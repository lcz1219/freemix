package com.freemix.freemix.service;

import com.freemix.freemix.enetiy.AIMessage;
import com.freemix.freemix.enetiy.User;
import com.freemix.freemix.util.ApiResponse;
import com.freemix.freemix.util.UserContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * AI消息服务类
 * 处理AI助手的对话记录持久化
 */
@Service
public class AIMessageService {

    @Autowired
    private MongoTemplate mongoTemplate;
    
    @Autowired
    private UserContextUtil userContextUtil;
    
    /**
     * 获取当前用户
     * @return 当前用户对象
     */
    private User getCurrentUser() {
        return userContextUtil.getCurrentUser();
    }
    
    /**
     * 获取当前用户名
     * @return 当前用户名
     */
    public String getCurrentUsername() {
        User user = getCurrentUser();
        return user != null ? user.getUsername() : null;
    }

    /**
     * 保存AI消息
     * @param aiMessage AI消息对象
     * @return ApiResponse
     */
    public ApiResponse<AIMessage> saveAIMessage(AIMessage aiMessage) {
        // 验证用户是否登录
//        String currentUser = getCurrentUsername();
//        if (currentUser == null) {
//            return ApiResponse.failure("用户未登录", 401);
//        }

        // 验证参数
        if (aiMessage.getUserQuestion() == null || aiMessage.getUserQuestion().trim().isEmpty()) {
            return ApiResponse.failure("用户问题不能为空", 400);
        }

        if (aiMessage.getAiAnswer() == null || aiMessage.getAiAnswer().trim().isEmpty()) {
            return ApiResponse.failure("AI回答不能为空", 400);
        }

        // 设置用户名
        aiMessage.setUsername(aiMessage.getUsername());
        // 设置创建时间
        if (aiMessage.getCreatedAt() == 0) {
            aiMessage.setCreatedAt(System.currentTimeMillis());
        }
        // 设置默认消息类型
        if (aiMessage.getMessageType() == null) {
            aiMessage.setMessageType("answer");
        }

        // 保存AI消息
        AIMessage savedMessage = mongoTemplate.save(aiMessage);
        return ApiResponse.success(savedMessage);
    }

    /**
     * 获取当前用户的所有AI消息记录
     * @return ApiResponse
     */
    public ApiResponse<List<AIMessage>> getAllAIMessages() {
        // 验证用户是否登录
        String currentUser = getCurrentUsername();
        if (currentUser == null) {
            return ApiResponse.failure("用户未登录", 401);
        }

        // 查询当前用户的所有AI消息，按时间倒序排列
        Query query = new Query(Criteria.where("username").is(currentUser))
                .with(Sort.by(Sort.Direction.DESC, "createdAt"));
        List<AIMessage> messages = mongoTemplate.find(query, AIMessage.class);
        return ApiResponse.success(messages);
    }

    /**
     * 获取当前用户的最近N条AI消息记录
     * @param limit 限制条数
     * @return ApiResponse
     */
    public ApiResponse<List<AIMessage>> getRecentAIMessages(int limit) {
        // 验证用户是否登录
        String currentUser = getCurrentUsername();
        if (currentUser == null) {
            return ApiResponse.failure("用户未登录", 401);
        }

        // 验证limit参数
        if (limit <= 0) {
            return ApiResponse.failure("限制条数必须大于0", 400);
        }

        // 查询当前用户的最近N条AI消息，按时间倒序排列
        Query query = new Query(Criteria.where("username").is(currentUser))
                .with(Sort.by(Sort.Direction.DESC, "createdAt"))
                .limit(limit);
        List<AIMessage> messages = mongoTemplate.find(query, AIMessage.class);
        return ApiResponse.success(messages);
    }

    /**
     * 获取当前用户在指定日期范围的AI消息记录
     * @param startTime 开始时间戳（毫秒）
     * @param endTime 结束时间戳（毫秒）
     * @return ApiResponse
     */
    public ApiResponse<List<AIMessage>> getAIMessagesByDateRange(long startTime, long endTime) {
        // 验证用户是否登录
        String currentUser = getCurrentUsername();
        if (currentUser == null) {
            return ApiResponse.failure("用户未登录", 401);
        }

        // 验证时间参数
        if (startTime <= 0 || endTime <= 0 || startTime >= endTime) {
            return ApiResponse.failure("时间范围参数无效", 400);
        }

        // 查询指定时间范围内的AI消息
        Query query = new Query(Criteria.where("username").is(currentUser)
                .and("createdAt").gte(startTime).lte(endTime))
                .with(Sort.by(Sort.Direction.DESC, "createdAt"));
        List<AIMessage> messages = mongoTemplate.find(query, AIMessage.class);
        return ApiResponse.success(messages);
    }

    /**
     * 删除指定的AI消息记录
     * @param messageId 消息ID
     * @return ApiResponse
     */
    public ApiResponse<String> deleteAIMessage(String messageId) {
        // 验证用户是否登录
        String currentUser = getCurrentUsername();
        if (currentUser == null) {
            return ApiResponse.failure("用户未登录", 401);
        }

        // 验证消息ID
        if (messageId == null || messageId.trim().isEmpty()) {
            return ApiResponse.failure("消息ID不能为空", 400);
        }

        // 查询消息是否存在且属于当前用户
        AIMessage message = mongoTemplate.findById(messageId, AIMessage.class);
        if (message == null) {
            return ApiResponse.failure("消息不存在", 404);
        }

        if (!message.getUsername().equals(currentUser)) {
            return ApiResponse.failure("无权限删除此消息", 403);
        }

        // 删除消息
        Query query = new Query(Criteria.where("id").is(messageId).and("username").is(currentUser));
        mongoTemplate.remove(query, AIMessage.class);
        
        return ApiResponse.success("AI消息删除成功");
    }

    /**
     * 清空当前用户的所有AI消息记录
     * @return ApiResponse
     */
    public ApiResponse<String> clearAllAIMessages() {
        // 验证用户是否登录
        String currentUser = getCurrentUsername();
        if (currentUser == null) {
            return ApiResponse.failure("用户未登录", 401);
        }

        // 删除当前用户的所有AI消息
        Query query = new Query(Criteria.where("username").is(currentUser));
        mongoTemplate.remove(query, AIMessage.class);
        
        return ApiResponse.success("清空AI消息记录成功");
    }

    /**
     * 获取当前用户的AI消息总数
     * @return ApiResponse
     */
    public ApiResponse<Long> getAIMessageCount() {
        // 验证用户是否登录
        String currentUser = getCurrentUsername();
        if (currentUser == null) {
            return ApiResponse.failure("用户未登录", 401);
        }

        // 统计当前用户的AI消息总数
        Query query = new Query(Criteria.where("username").is(currentUser));
        long count = mongoTemplate.count(query, AIMessage.class);
        
        return ApiResponse.success(count);
    }
    
    /**
     * 获取指定用户的历史AI消息记录
     * @param username 用户名
     * @return ApiResponse
     */
    public ApiResponse<List<AIMessage>> getUserHistory(String username) {
        // 验证用户名参数
        if (username == null || username.trim().isEmpty()) {
            return ApiResponse.failure("用户名不能为空", 400);
        }

        // 查询指定用户的所有AI消息，按时间正序排列（时间早的在前面）
        Query query = new Query(Criteria.where("username").is(username))
                .with(Sort.by(Sort.Direction.ASC, "createdAt"));
        List<AIMessage> messages = mongoTemplate.find(query, AIMessage.class);
        
        return ApiResponse.success(messages);
    }
}