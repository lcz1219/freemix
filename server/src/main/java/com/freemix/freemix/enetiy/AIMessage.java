package com.freemix.freemix.enetiy;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * AI消息实体类
 * 用于存储用户与AI助手的对话记录
 */
@Data
@Document(collection = "aiMsgs")
public class AIMessage {
    /**
     * 消息ID，MongoDB自动生成
     */
    @Id
    private String id;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 用户问题
     */
    private String userQuestion;
    
    /**
     * AI回答内容
     */
    private String aiAnswer;
    
    /**
     * AI思考过程内容（可选）
     */
    private String thinkingContent;
    
    /**
     * 推荐问题列表（可选）
     */
    private List<String> followUpQuestions;
    
    /**
     * 消息类型：answer（普通回答）、verbose（详细思考过程）、follow_up（推荐问题）
     */
    private String messageType;
    
    /**
     * 消息创建时间戳（毫秒）
     */
    private long createdAt;

    /**
     * 默认构造函数
     */
    public AIMessage() {
        this.createdAt = System.currentTimeMillis();
    }

    /**
     * 全参数构造函数
     * @param username 用户名
     * @param userQuestion 用户问题
     * @param aiAnswer AI回答
     */
    public AIMessage(String username, String userQuestion, String aiAnswer) {
        this.username = username;
        this.userQuestion = userQuestion;
        this.aiAnswer = aiAnswer;
        this.messageType = "answer";
        this.createdAt = System.currentTimeMillis();
    }

    /**
     * 完整参数构造函数
     * @param username 用户名
     * @param userQuestion 用户问题
     * @param aiAnswer AI回答
     * @param thinkingContent 思考内容
     * @param followUpQuestions 推荐问题
     * @param messageType 消息类型
     */
    public AIMessage(String username, String userQuestion, String aiAnswer, 
                    String thinkingContent, List<String> followUpQuestions, String messageType) {
        this.username = username;
        this.userQuestion = userQuestion;
        this.aiAnswer = aiAnswer;
        this.thinkingContent = thinkingContent;
        this.followUpQuestions = followUpQuestions;
        this.messageType = messageType;
        this.createdAt = System.currentTimeMillis();
    }
}