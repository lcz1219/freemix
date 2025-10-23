package com.freemix.freemix.service;

import com.freemix.freemix.enetiy.Message;
import com.freemix.freemix.enetiy.User;
import com.freemix.freemix.util.ApiResponse;
import com.freemix.freemix.util.UserContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MessageService {

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
     * 发送消息
     * @param message 消息对象
     * @return ApiResponse
     */
    public ApiResponse<Message> sendMessage(Message message) {
        // 验证用户是否登录
        String currentUser = getCurrentUsername();
        if (currentUser == null) {
            return  ApiResponse.failure("用户未登录");
        }

        // 验证参数
        if (message.getToUser() == null || message.getToUser().trim().isEmpty()) {
            return ApiResponse.failure("接收者不能为空");
        }

        if (message.getContent() == null || message.getContent().trim().isEmpty()) {
            return ApiResponse.failure("消息内容不能为空");
        }

        // 设置消息发送者
        message.setFromUser(currentUser);
        // 设置创建时间
        message.setCreatedAt(System.currentTimeMillis());
        // 设置默认消息类型为文本
        if (message.getType() == null) {
            message.setType("text");
        }
        // 默认未读
        message.setRead(false);

        // 保存消息
        Message savedMessage = mongoTemplate.save(message);
        return ApiResponse.success( savedMessage);
    }

    /**
     * 获取收到的消息
     * @return ApiResponse
     */
    public ApiResponse<List<Message>> getReceivedMessages() {
        // 验证用户是否登录
        String currentUser = getCurrentUsername();
        if (currentUser == null) {
            return ApiResponse.failure("用户未登录");
        }

        // 查询收到的消息，按时间倒序排列
        Query query = new Query(Criteria.where("toUser").is(currentUser))
                .with(Sort.by(Sort.Direction.DESC, "createdAt"));
        List<Message> messages = mongoTemplate.find(query, Message.class);
        return ApiResponse.success( messages);
    }

    /**
     * 获取发送的消息
     * @return ApiResponse
     */
    public ApiResponse<List<Message>> getSentMessages() {
        // 验证用户是否登录
        String currentUser = getCurrentUsername();
        if (currentUser == null) {
            return ApiResponse.failure("用户未登录", 401);
        }

        // 查询发送的消息，按时间倒序排列
        Query query = new Query(Criteria.where("fromUser").is(currentUser))
                .with(Sort.by(Sort.Direction.DESC, "createdAt"));
        List<Message> messages = mongoTemplate.find(query, Message.class);
        return ApiResponse.success( messages);
    }

    /**
     * 标记消息为已读
     * @param messageId 消息ID
     * @return ApiResponse
     */
    public ApiResponse<Message> markAsRead(String messageId) {
        // 验证用户是否登录
        String currentUser = getCurrentUsername();
        if (currentUser == null) {
            return ApiResponse.failure("用户未登录", 401);
        }

        // 查询消息
        Message message = mongoTemplate.findById(messageId, Message.class);
        if (message == null) {
            return ApiResponse.failure("消息不存在", 404);
        }

        // 验证消息接收者是否为当前用户
        if (!message.getToUser().equals(currentUser)) {
            return ApiResponse.failure("无权限操作此消息", 403);
        }

        // 更新消息状态
        message.setRead(true);
        Message updatedMessage = mongoTemplate.save(message);
        return ApiResponse.success( updatedMessage);
    }

    /**
     * 批量标记消息为已读
     * @param messageIds 消息ID列表
     * @return ApiResponse
     */
    public ApiResponse<String> markAsReadBatch(List<String> messageIds) {
        // 验证用户是否登录
        String currentUser = getCurrentUsername();
        if (currentUser == null) {
            return ApiResponse.failure("用户未登录", 401);
        }

        if (messageIds == null || messageIds.isEmpty()) {
            return ApiResponse.failure("消息ID列表不能为空", 400);
        }

        // 批量更新消息状态
        Query query = new Query(Criteria.where("id").in(messageIds).and("toUser").is(currentUser));
        Update update = new Update().set("isRead", true);
        mongoTemplate.updateMulti(query, update, Message.class);

        return ApiResponse.success( "成功标记 " + messageIds.size() + " 条消息为已读");
    }

    /**
     * 获取与指定用户的聊天记录
     * @param username 用户名
     * @return ApiResponse
     */
    public ApiResponse<List<Message>> getChatHistory(String username) {
        // 验证用户是否登录
        String currentUser = getCurrentUsername();
        if (currentUser == null) {
            return ApiResponse.failure("用户未登录", 401);
        }

        // 查询与指定用户的聊天记录
        Criteria criteria = new Criteria().orOperator(
                Criteria.where("fromUser").is(currentUser).and("toUser").is(username),
                Criteria.where("fromUser").is(username).and("toUser").is(currentUser)
        );
        
        Query query = new Query(criteria)
                .with(Sort.by(Sort.Direction.ASC, "createdAt"));
        List<Message> messages = mongoTemplate.find(query, Message.class);

        return ApiResponse.success( messages);
    }

    /**
     * 获取未读消息数量
     * @return ApiResponse
     */
    public ApiResponse getUnreadCount() {
        // 验证用户是否登录
        String currentUser = getCurrentUsername();
        if (currentUser == null) {
            return ApiResponse.failure("用户未登录", 401);
        }

        // 查询未读消息数量
        Query query = new Query(Criteria.where("toUser").is(currentUser).and("isRead").is(false));
//        long count = mongoTemplate.count(query, Message.class);
        List<Message> messages = mongoTemplate.find(query, Message.class);
        Set<String> collect = messages.stream().map(Message::getFromUser).collect(Collectors.toSet());
        Map<String, Long>map = new HashMap<>();
        collect.forEach(e->{
            long count1 = messages.stream().filter(m -> m.getFromUser().equals(e)).count();
            map.put(e,count1);
        });

        return ApiResponse.success( map);
    }

    /**
     * 获取未读消息内容
     * @return ApiResponse
     */
    public ApiResponse<List<Message>> getUnreadMessages() {
        // 验证用户是否登录
        String currentUser = getCurrentUsername();
        if (currentUser == null) {
            return ApiResponse.failure("用户未登录", 401);
        }

        // 查询未读消息
        Query query = new Query(Criteria.where("toUser").is(currentUser).and("isRead").is(false));
        List<Message> messages = mongoTemplate.find(query, Message.class);
        
        return ApiResponse.success(messages);
    }

    /**
     * 保存消息
     * @param message 消息对象
     * @return 保存后的消息对象
     */
    public Message saveMessage(Message message) {
        return mongoTemplate.save(message);
    }
    
    /**
     * 删除消息
     * @param messageId 消息ID
     * @return ApiResponse
     */
    public ApiResponse<String> deleteMessage(String messageId) {
        // 验证用户是否登录
        String currentUser = getCurrentUsername();
        if (currentUser == null) {
            return ApiResponse.failure("用户未登录", 401);
        }

        // 查询消息
        Message message = mongoTemplate.findById(messageId, Message.class);
        if (message == null) {
            return ApiResponse.failure("消息不存在", 404);
        }

        // 验证消息发送者或接收者是否为当前用户
        if (!message.getFromUser().equals(currentUser) && !message.getToUser().equals(currentUser)) {
            return ApiResponse.failure("无权限操作此消息", 403);
        }

        // 删除消息
        mongoTemplate.remove(message);
        return ApiResponse.success( "消息删除成功");
    }
}