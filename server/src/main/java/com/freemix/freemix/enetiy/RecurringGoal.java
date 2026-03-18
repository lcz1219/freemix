package com.freemix.freemix.enetiy;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Document(value = "recurring_goal")
@Data
public class RecurringGoal {
    String _id;
    String title;
    String description;
    String owner;
    
    /**
     * 循环类型: daily, weekly, monthly, cron
     */
    String recurrenceType;
    
    /**
     * Cron 表达式 (可选)
     */
    String cronExpression;
    
    /**
     * 循环配置: 
     * daily: 无需额外配置或指定时间点
     * weekly: 1-7 (周一到周日)
     * monthly: 1-31 (几号)
     */
    String recurrenceConfig;
    
    Date nextExecutionTime;
    Date lastGeneratedTime;
    Boolean isActive = true;
    
    // 模板字段，用于生成真正的 Goal
    String level;
    List<String> tags = new ArrayList<>();
    List<childGoals> childGoals = new ArrayList<>();
    String richText;
    Boolean isPublic = false;
    
    Date createTime;
    Integer del = 0; // 0: 正常, 1: 已删除
}
