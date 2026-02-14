package com.freemix.freemix.enetiy;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Document(collection = "apiLog")
public class ApiLog {
    @Id
    private String id;
    
    private String username;    // 操作人
    private String ip;          // 请求IP
    private String url;         // 请求路径
    private String method;      // 请求方式
    private String classMethod; // 类方法
    private Object args;        // 请求参数
    private Object result;      // 返回结果
    private Long spendTime;     // 消耗时间(ms)
    private Date createTime;    // 创建时间
    private String createTimeStr;    // 创建时间
}
