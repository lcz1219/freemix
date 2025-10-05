package com.freemix.freemix.controller;

import com.freemix.freemix.CheckToken;
import com.freemix.freemix.enetiy.User;
import com.freemix.freemix.util.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户相关接口控制器
 */
@RestController
public class UserController extends BaseController {
    
//    /**
//     * 获取当前登录用户信息
//     * @return ApiResponse 包含用户信息的响应
//     */
//    @GetMapping("/user/current")
//    @CheckToken
//    public ApiResponse getCurrentUser() {
//        User user = getCurrentUser();
//        if (user != null) {
//            // 清除敏感信息
//            user.setPassword(null);
//            return ApiResponse.success(user);
//        } else {
//            return ApiResponse.failure("未找到用户信息");
//        }
//    }
}