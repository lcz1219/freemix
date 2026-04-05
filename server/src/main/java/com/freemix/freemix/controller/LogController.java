package com.freemix.freemix.controller;

import com.freemix.freemix.CheckToken;
import com.freemix.freemix.enetiy.ApiLog;
import com.freemix.freemix.enetiy.User;
import com.freemix.freemix.util.ApiResponse;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/logs")
public class LogController extends BaseController {

    private static final String ADMIN_EMAIL = "1033519224@qq.com";

    @GetMapping("/list")
    @CheckToken
    public ApiResponse getLogList(@RequestParam(defaultValue = "1") int page,
                                 @RequestParam(defaultValue = "20") int size,
                                 @RequestParam(required = false) String username,
                                 @RequestParam(required = false) Integer status,
                                 @RequestParam(required = false) String url) {
        
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ApiResponse.failure("用户未登录");
        }

        if (!ADMIN_EMAIL.equals(currentUser.getEmail())) {
            log.warn("用户 {} 尝试无权访问日志管理", currentUser.getUsername());
            return ApiResponse.failure("您没有权限查看系统日志", 403);
        }

        Query query = new Query();
        if (username != null && !username.isEmpty()) {
            query.addCriteria(Criteria.where("username").regex(username, "i"));
        }
        if (status != null) {
            if (status == 200) {
                query.addCriteria(Criteria.where("result.code").is(200));
            } else {
                query.addCriteria(Criteria.where("result.code").ne(200));
            }
        }
        if (url != null && !url.isEmpty()) {
            query.addCriteria(Criteria.where("classMethod").regex(url, "i"));
        }

        long total = mongoTemplate.count(query, ApiLog.class);
        
        // 统计信息
        Query statsQuery = new Query();
        if (username != null && !username.isEmpty()) {
            statsQuery.addCriteria(Criteria.where("username").regex(username, "i"));
        }
        if (url != null && !url.isEmpty()) {
            statsQuery.addCriteria(Criteria.where("classMethod").regex(url, "i"));
        }
        
        long successCount = mongoTemplate.count(Query.of(statsQuery).addCriteria(Criteria.where("result.code").is(200)), ApiLog.class);
        long failCount = mongoTemplate.count(Query.of(statsQuery).addCriteria(Criteria.where("result.code").ne(200)), ApiLog.class);

        query.with(Sort.by(Sort.Direction.DESC, "createTime"));
        query.with(Sort.by(Sort.Direction.DESC, "createTimeStr"));
        query.with(PageRequest.of(page - 1, size));

        List<ApiLog> logs = mongoTemplate.find(query, ApiLog.class);
        
        JSONObject result = new JSONObject();
        result.put("list", logs);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("successCount", successCount);
        result.put("failCount", failCount);

        return ApiResponse.success(result);
    }

    @DeleteMapping("/clear")
    @CheckToken
    public ApiResponse clearLogs() {
        User currentUser = getCurrentUser();
        if (currentUser == null || !ADMIN_EMAIL.equals(currentUser.getEmail())) {
            return ApiResponse.failure("无权操作", 403);
        }

        mongoTemplate.remove(new Query(), ApiLog.class);
        log.info("管理员 {} 清空了所有系统日志", currentUser.getUsername());
        return ApiResponse.success("日志已清空");
    }
}
