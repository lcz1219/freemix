package com.freemix.freemix.enetiy;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginLog {
    private String id;
    private String userId;          // 用户ID
    private String username;        // 用户名
    private String ipAddress;       // 登录IP地址
    private String userAgent;       // 用户代理信息
    private boolean loginSuccess;   // 登录是否成功
    private String errorMessage;    // 失败时的错误信息
    private Date loginTime;         // 登录时间
    private String deviceType;      // 设备类型（PC/Mobile/Other）
    private String browser;         // 浏览器信息
    private String os;              // 操作系统信息
}