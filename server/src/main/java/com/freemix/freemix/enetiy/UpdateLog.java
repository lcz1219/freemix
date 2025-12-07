package com.freemix.freemix.enetiy;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.Set;

@Data
@Document(collection = "updatelog")
public class UpdateLog {
    @Id
    private String id;
    
    // 版本号 (语义化版本)
    private String version;
    
    // 更新日期
    private Date updateTime;
    
    // 更新内容 (Markdown)
    private String content;
    
    // 影响范围
    private String scope;
    
    // 重要程度: low, medium, high
    private String importance;
    
    // 创建时间
    private Date createdAt;
    
    // 是否已发布
    private boolean published;
    //已阅读用户
    Set<String> readUsers;
}
