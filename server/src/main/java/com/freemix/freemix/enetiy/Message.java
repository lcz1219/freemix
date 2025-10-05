package com.freemix.freemix.enetiy;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * 消息实体类
 * 用于存储用户之间的消息通信记录
 */
@Data
@Document(collection = "messages")
public class Message {
    /**
     * 消息ID，MongoDB自动生成
     */
    @Id
    private String id;
    
    /**
     * 发送者用户名
     */
    private String fromUser;
    
    /**
     * 接收者用户名
     */
    private String toUser;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 消息是否已读
     */
    private boolean isRead;
    
    /**
     * 消息类型，如text(文本), image(图片)等
     */
    private String type;
    
    /**
     * 消息创建时间戳（毫秒）
     */
    private long createdAt;

    /**
     * 默认构造函数
     * 设置默认消息类型为文本，状态为未读
     */
    public Message() {
        this.type = "text"; // 默认消息类型为文本
        this.isRead = false; // 默认未读
        this.createdAt = System.currentTimeMillis(); // 初始化创建时间
    }

    /**
     * 全参数构造函数
     * @param fromUser 发送者
     * @param toUser 接收者
     * @param content 消息内容
     * @param type 消息类型
     */
    public Message(String fromUser, String toUser, String content, String type) {
        this.fromUser = fromUser;
        this.toUser = toUser;
        this.content = content;
        this.type = type != null ? type : "text";
        this.isRead = false;
        this.createdAt = System.currentTimeMillis(); // 初始化创建时间
    }
}