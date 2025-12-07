package com.freemix.freemix.controller;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.freemix.freemix.enetiy.UpdateLog;
import com.freemix.freemix.service.UpdateLogService;
import com.freemix.freemix.util.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/updates")
public class UpdateLogController {

    @Autowired
    private UpdateLogService updateLogService;
    @Autowired
    private MongoTemplate mongoTemplate;

    // 获取最新更新日志 (用户端)
    @GetMapping("/latest")
    public ApiResponse<UpdateLog> getLatestUpdate() {
        UpdateLog log = updateLogService.getLatestUpdateLog();
        Set<String> objects = new HashSet<>();

        if(log.getReadUsers() == null) {
            log.setReadUsers(objects);
        }
        return ApiResponse.success(log);
    }

    // 获取所有更新日志 (管理员/历史记录)
    @GetMapping("/list")
    public ApiResponse<List<UpdateLog>> getAllUpdates(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(updateLogService.getAllUpdateLogs(page, size));
    }

    // 创建更新日志 (仅管理员 lcz)
    @PostMapping("/create")
    public ApiResponse<UpdateLog> createUpdate(@RequestBody UpdateLog updateLog, @RequestParam String username) {
        if (isABoolean(username)) {
            return ApiResponse.failure("无权操作");
        }
        return ApiResponse.success(updateLogService.createUpdateLog(updateLog));
    }

    private static boolean isABoolean(String username) {
        return !"lcz".equals(username) && !"linchengzhong".equals(username);
    }

    // 发布更新日志 (仅管理员 lcz) - 触发WebSocket推送逻辑可在前端调用或在此处集成
    @PostMapping("/publish/{id}")
    public ApiResponse<UpdateLog> publishUpdate(@PathVariable String id, @RequestParam String username) {
        if (isABoolean(username)) {
            return ApiResponse.failure("无权操作");
        }
        return ApiResponse.success(updateLogService.publishUpdateLog(id));
    }
    @PostMapping("isread")
    public ApiResponse<Boolean> isRead(@RequestBody String body) {
        UpdateLog updateLog = JSONObject.parseObject(body, UpdateLog.class);
        mongoTemplate.save(updateLog);
        return ApiResponse.success(true);

    }

    // 删除更新日志 (仅管理员 lcz)
    @GetMapping("/{id}")
    public ApiResponse<String> deleteUpdate(@PathVariable String id, @RequestParam String username) {
        if (isABoolean(username)) {
            return ApiResponse.failure("无权操作");
        }
        updateLogService.deleteUpdateLog(id);
        return ApiResponse.success("Deleted successfully");
    }
}
