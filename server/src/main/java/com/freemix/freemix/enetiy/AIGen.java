package com.freemix.freemix.enetiy;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * AI生成记录实体类
 * 用于存储AI目标生成的记录，支持30天内延迟决策和分享功能
 */
@Data
@Document(collection = "aiGen")
public class AIGen {
    
    /**
     * 记录ID，MongoDB自动生成
     */
    @Id
    private String id;
    
    /**
     * 用户ID
     */
    private String userId;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 用户输入的目标描述
     */
    private String userInput;
    
    /**
     * AI生成的完整响应内容
     */
    private String aiResponse;
    
    /**
     * 生成的目标标题
     */
    private String goalTitle;
    
    /**
     * 子目标列表
     */
    private List<SubGoal> childGoals;
    
    /**
     * 生成状态：pending(待确认)、confirmed(已确认)、expired(已过期)
     */
    private String status;
    
    /**
     * 分享token，用于生成分享链接
     */
    private String shareToken;
    
    /**
     * 分享状态：private(私有)、shared(已分享)
     */
    private String shareStatus;
    
    /**
     * 记录创建时间戳（毫秒）
     */
    private long createdAt;
    
    /**
     * 记录过期时间戳（毫秒），30天后过期
     */
    private long expireAt;
    
    /**
     * 确认时间戳（毫秒）
     */
    private long confirmedAt;
    
    /**
     * 分享时间戳（毫秒）
     */
    private long sharedAt;
    
    /**
     * 聊天消息记录，用于完整展示对话过程
     */
    private List<ChatMessage> chatMessages;
    
    /**
     * 子目标内部类
     */
    @Data
    public static class SubGoal {
        private int step;
        private String message;
        
        public SubGoal() {}
        
        public SubGoal(int step, String message) {
            this.step = step;
            this.message = message;
        }
    }
    
    /**
     * 聊天消息内部类
     */
    @Data
    public static class ChatMessage {
        private String type;
        private String messageType;
        private String content;
        private String thinkingContent;
        private boolean isProcessing;
        private long timestamp;
        
        public ChatMessage() {}
        
        public ChatMessage(String type, String content, long timestamp) {
            this.type = type;
            this.content = content;
            this.timestamp = timestamp;
        }
    }
    
    /**
     * 默认构造函数
     */
    public AIGen() {
        this.createdAt = System.currentTimeMillis();
        this.expireAt = this.createdAt + (30L * 24 * 60 * 60 * 1000); // 30天后过期
        this.status = "pending";
        this.shareStatus = "private";
    }
    
    /**
     * 生成分享token
     */
    public void generateShareToken() {
        this.shareToken = java.util.UUID.randomUUID().toString().replace("-", "");
        this.shareStatus = "shared";
        this.sharedAt = System.currentTimeMillis();
    }
    
    /**
     * 确认生成记录
     */
    public void confirm() {
        this.status = "confirmed";
        this.confirmedAt = System.currentTimeMillis();
    }
    
    /**
     * 检查是否已过期
     */
    public boolean isExpired() {
        return System.currentTimeMillis() > this.expireAt;
    }
    
    /**
     * 获取剩余天数
     */
    public int getRemainingDays() {
        long remainingTime = this.expireAt - System.currentTimeMillis();
        return remainingTime > 0 ? (int) Math.ceil(remainingTime / (24.0 * 60 * 60 * 1000)) : 0;
    }
}