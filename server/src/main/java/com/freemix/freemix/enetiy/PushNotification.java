package com.freemix.freemix.enetiy;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * 推送通知实体类
 * 用于存储从 iOS 原生推送过来的系统通知消息
 * （目标到期提醒、AI晨报、每日概览等）
 */
@Data
@Document(collection = "PushNotifications")
public class PushNotification {
    @Id
    private String id;

    /** 接收通知的用户名 */
    private String username;

    /** 通知标题 */
    private String title;

    /** 通知正文内容 */
    private String body;

    /** 通知类型：goal_reminder / ai_morning / daily_summary / system */
    private String type;

    /** 关联的目标ID（如果是目标提醒类通知） */
    private String goalId;

    /** 关联的目标标题 */
    private String goalTitle;

    /** 是否已读 */
    private boolean isRead;

    /** 通知创建时间（毫秒时间戳） */
    private long createdAt;

    /** 服务端记录时间 */
    private Date serverTime;

    public PushNotification() {
        this.isRead = false;
        this.createdAt = System.currentTimeMillis();
        this.serverTime = new Date();
    }
}
