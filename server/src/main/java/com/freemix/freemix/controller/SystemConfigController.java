package com.freemix.freemix.controller;

import com.alibaba.fastjson2.JSONObject;
import com.freemix.freemix.CheckToken;
import com.freemix.freemix.enetiy.SystemConfig;
import com.freemix.freemix.enetiy.User;
import com.freemix.freemix.util.ApiResponse;
import com.freemix.freemix.util.UserContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/config")
@CrossOrigin(origins = "*")
public class SystemConfigController {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private UserContextUtil userContextUtil;

    private static final String CONFIG_TYPE = "DOWNLOAD_CONFIG";
    private static final String ADMIN_EMAIL = "1033519224@qq.com";

    /**
     * 获取下载配置 (公开接口)
     */
    @GetMapping("/downloads")
    public ApiResponse getDownloadConfig() {
        SystemConfig config = mongoTemplate.findOne(
                new Query().addCriteria(Criteria.where("type").is(CONFIG_TYPE)),
                SystemConfig.class
        );

        if (config == null) {
            // 返回默认值，防止空指针
            config = new SystemConfig();
            config.setType(CONFIG_TYPE);
            config.setMacDownloadUrl("https://github.com/lcz1219/freemix/releases/download/freemix-v0.0.3/FreeMix-0.0.0-arm64.dmg");
            config.setWinDownloadUrl("https://github.com/lcz1219/freemix/releases/download/freemix-v0.0.3/FreeMix-0.0.0-win.zip");
        }

        return ApiResponse.success(config);
    }

    /**
     * 更新下载配置 (需权限)
     */
    @PostMapping("/downloadsp")
    @CheckToken
    public ApiResponse updateDownloadConfig(@RequestBody SystemConfig newConfig) {
        User currentUser = userContextUtil.getCurrentUser();
        if (currentUser == null) {
            return ApiResponse.failure("用户未登录");
        }

        if (!ADMIN_EMAIL.equals(currentUser.getEmail())) {
            return ApiResponse.failure("无权操作：仅限管理员修改");
        }

        // 查找现有配置
        SystemConfig existingConfig = mongoTemplate.findOne(
                new Query().addCriteria(Criteria.where("type").is(CONFIG_TYPE)),
                SystemConfig.class
        );

        if (existingConfig == null) {
            existingConfig = new SystemConfig();
            existingConfig.setType(CONFIG_TYPE);
        }

        // 更新字段
        existingConfig.setMacDownloadUrl(newConfig.getMacDownloadUrl());
        existingConfig.setWinDownloadUrl(newConfig.getWinDownloadUrl());

        mongoTemplate.save(existingConfig);

        return ApiResponse.success(existingConfig);
    }
}
