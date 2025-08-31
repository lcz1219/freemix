package com.freemix.freemix.service;

import com.freemix.freemix.enetiy.LoginLog;
import java.util.List;
import java.util.Date;

public interface LoginLogService {
    /**
     * 保存登录日志
     */
    void saveLoginLog(LoginLog loginLog);
    
    /**
     * 根据用户ID查询登录日志
     */
    List<LoginLog> getLoginLogsByUserId(String userId, int page, int size);
    
    /**
     * 查询指定日期范围内的登录日志
     */
    List<LoginLog> getLoginLogsByDateRange(Date startDate, Date endDate, int page, int size);
    
    /**
     * 查询最近的登录失败记录
     */
    List<LoginLog> getRecentFailedLoginAttempts(String username, int count);
}