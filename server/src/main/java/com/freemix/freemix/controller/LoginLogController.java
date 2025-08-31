package com.freemix.freemix.controller;

import com.freemix.freemix.service.LoginLogService;
import com.freemix.freemix.util.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
@RestController
@RequestMapping("/api/login-log")

public class LoginLogController {

    @Autowired
    private LoginLogService loginLogService;

    /**
     * 根据用户ID查询登录日志
     */
    @GetMapping("/user")
    public ApiResponse getUserLoginLogs(
            @RequestParam String userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            return ApiResponse.success(loginLogService.getLoginLogsByUserId(userId, page, size));
        } catch (Exception e) {
            log.error("查询用户登录日志失败: {}", e.getMessage(), e);
            return ApiResponse.failure("查询登录日志失败");
        }
    }

    /**
     * 查询指定日期范围内的登录日志
     */
    @GetMapping("/date-range")
    public ApiResponse getLoginLogsByDateRange(
            @RequestParam String startDate, // 格式: yyyy-MM-dd
            @RequestParam String endDate,   // 格式: yyyy-MM-dd
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date start = sdf.parse(startDate);
            // 将结束日期设置为当天的23:59:59
            Date end = new Date(sdf.parse(endDate).getTime() + 24 * 60 * 60 * 1000 - 1);
            
            return ApiResponse.success(loginLogService.getLoginLogsByDateRange(start, end, page, size));
        } catch (ParseException e) {
            log.error("日期格式解析错误: {}", e.getMessage());
            return ApiResponse.failure("日期格式错误，请使用yyyy-MM-dd格式");
        } catch (Exception e) {
            log.error("查询日期范围登录日志失败: {}", e.getMessage(), e);
            return ApiResponse.failure("查询登录日志失败");
        }
    }

    /**
     * 查询最近的登录失败记录
     */
    @GetMapping("/recent-failed")
    public ApiResponse getRecentFailedLoginAttempts(
            @RequestParam String username,
            @RequestParam(defaultValue = "10") int count) {
        try {
            return ApiResponse.success(loginLogService.getRecentFailedLoginAttempts(username, count));
        } catch (Exception e) {
            log.error("查询最近登录失败记录失败: {}", e.getMessage(), e);
            return ApiResponse.failure("查询登录日志失败");
        }
    }
}