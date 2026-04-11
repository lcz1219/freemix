package com.freemix.freemix.controller;

import com.freemix.freemix.CheckToken;
import com.freemix.freemix.enetiy.ApiLog;
import com.freemix.freemix.enetiy.User;
import com.freemix.freemix.util.ApiResponse;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.redis.core.RedisTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/logs")
public class LogController extends BaseController {

    private static final String ADMIN_EMAIL = "1033519224@qq.com";

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;

    private static final String CLASS_METHODS_CACHE_KEY = "api_logs:class_methods";

    /**
     * 获取日志列表（只返回最新的10000条数据）
     */
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

        Criteria successCriteria = new Criteria().orOperator(
                Criteria.where("result.code").is(200),
                Criteria.where("result.code").is(null)
        );

        Criteria failCriteria = new Criteria().andOperator(
                Criteria.where("result.code").nin(200, null)
        );
        Date timeThreshold = getTimeThresholdForLatest10000();

        // 1. 异步查询：分页列表
        CompletableFuture<List<ApiLog>> logsFuture = CompletableFuture.supplyAsync(() -> {
            Query query = buildBaseQuery(username, url,timeThreshold);
            if (status != null) {
                query.addCriteria(status == 200 ? successCriteria : failCriteria);
            }
//            query.with(Sort.by(Sort.Direction.DESC, "createTimeStr"));

            query.with(PageRequest.of(page - 1, size));
            return mongoTemplate.find(query, ApiLog.class);
        });

        // 2. 异步查询：Total 总数（限制最多10000条）
//        CompletableFuture<Long> totalFuture = CompletableFuture.supplyAsync(() -> {
//            Query query = buildBaseQuery(username, url,timeThreshold);
//            if (status != null) {
//                query.addCriteria(status == 200 ? successCriteria : failCriteria);
//            }
//            long count = mongoTemplate.count(query, ApiLog.class);
//            return Math.min(count, 10000L); // 限制总数不超过10000
//        });

        // 3. 异步查询：SuccessCount (只有当 status == null 时才需要真正去查底层，限制最多10000条)
        CompletableFuture<Long> successCountFuture = CompletableFuture.supplyAsync(() -> {
//            if (status != null) {
//                return 0L; // 如果 status 不为空，这里先随便返回一个0，后面通过数学计算得出，不去查库
//            }
            Query query = buildBaseQuery(username, url,timeThreshold);
            query.addCriteria(successCriteria);
            long count = mongoTemplate.count(query, ApiLog.class);
            return Math.min(count, 10000L); // 限制成功数不超过10000
        });
        CompletableFuture<Long> failCountFuture = CompletableFuture.supplyAsync(() -> {
//            if (status != null) {
//                return 0L; // 如果 status 不为空，这里先随便返回一个0，后面通过数学计算得出，不去查库
//            }
            Query query = buildBaseQuery(username, url,timeThreshold);
            query.addCriteria(failCriteria);
            long count = mongoTemplate.count(query, ApiLog.class);
            return Math.min(count, 10000L); // 限制成功数不超过10000
        });

        try {
            // 阻塞等待所有并发查询完成 (总耗时 = 耗时最长的那个查询)
//            CompletableFuture.allOf(logsFuture, totalFuture, successCountFuture).join();

            List<ApiLog> logs = logsFuture.get();
//            long total = totalFuture.get();
            long dbSuccessCount = successCountFuture.get();

            long successCount = 0;
            long failCount = 0;

            // 根据数学逻辑推导最终的 count，严格保持逻辑不变
//            if (status != null) {
//                if (status == 200) {
//                    successCount = total;
//                    failCount = 0;
//                } else {
//                    successCount = 0;
//                    failCount = total;
//                }
//            } else {
                successCount = dbSuccessCount;
//                failCount = total - successCount;
                failCount = failCountFuture.get();
//            }

            JSONObject result = new JSONObject();
            result.put("list", logs);
            result.put("total", successCount+failCount);
            result.put("page", page);
            result.put("size", size);
            result.put("successCount", successCount);
            result.put("failCount", failCount);

            return ApiResponse.success(result);

        } catch (Exception e) {
            log.error("查询日志列表异常", e);
            return ApiResponse.failure("系统繁忙，请稍后再试");
        }
    }
    private static final List<String> EXCLUDED_METHODS = Arrays.asList("com.freemix.freemix.controller.UserStatusController.getAllUserStatus");


    // --- 提取一个公用方法，保证各线程拥有独立且干净的 Query 对象，避免线程安全问题 ---
    private Query buildBaseQuery(String username, String url,Date timeThreshold) {
        Query query = new Query();
// 计算一个月前的日期字符串（格式必须与 createTimeStr 一致："yyyy-MM-dd HH:mm:ss"）
//        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
//        String startDateStr = oneMonthAgo.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
//
//        // 字符串范围比较（注意：这无法使用索引，会全表扫描）
//        query.addCriteria(Criteria.where("createTimeStr").gte(startDateStr));
        // 排除 classMethod 在排除列表中的文档
        if (!EXCLUDED_METHODS.isEmpty()) {
            query.addCriteria(Criteria.where("classMethod").nin(EXCLUDED_METHODS));
        }
        if (url != null && !url.isEmpty()) {
            // 修改为精确匹配，因为前端现在改成了下拉框
            query.addCriteria(Criteria.where("classMethod").is(url));
        }
        if (timeThreshold != null) {
            query.addCriteria(Criteria.where("createTime").gte(timeThreshold));
        }
        return query;
    }
    private Date getTimeThresholdForLatest10000() {
        // 查询第 10000 条记录（跳过 9999 条，取 1 条）
        Query query = new Query()
                .with(Sort.by(Sort.Direction.DESC, "createTime"))
                .limit(1)
                .skip(2999);
        // 只投影 createTime 字段，减少数据传输
        query.fields().include("createTime");
        ApiLog log = mongoTemplate.findOne(query, ApiLog.class);
        if (log != null && log.getCreateTime() != null) {
            return log.getCreateTime();
        }
        return null; // 总数不足 10000，不限制时间
    }
    @GetMapping("/classMethods")
    @CheckToken
    public ApiResponse getClassMethods() {
        User currentUser = getCurrentUser();
        if (currentUser == null || !ADMIN_EMAIL.equals(currentUser.getEmail())) {
            return ApiResponse.failure("无权访问", 403);
        }

        try {
            // 1. 尝试从 Redis 获取缓存 (拿到的是 JSON 字符串)
            Object cachedData = redisTemplate.opsForValue().get(CLASS_METHODS_CACHE_KEY);
            if (cachedData != null) {
                log.info("从 Redis 缓存获取 classMethod 列表");
                // 将 JSON 字符串反序列化为 List<String>
                List<String> cachedMethods = objectMapper.readValue(
                        cachedData.toString(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
                );
                return ApiResponse.success(cachedMethods);
            }

            // 2. 缓存不存在，从 MongoDB 查询
            log.info("Redis 缓存失效，从 MongoDB 查询 classMethod 列表");
            List<String> classMethods = mongoTemplate.getCollection(mongoTemplate.getCollectionName(ApiLog.class))
                    .distinct("classMethod", String.class)
                    .into(new java.util.ArrayList<>());

            // 过滤并排序
            List<String> result = classMethods.stream()
                    .filter(s -> s != null && !s.isEmpty())
                    .sorted()
                    .collect(Collectors.toList());

            // 3. 将 List 转换为 JSON 字符串后存入 Redis，设置 2 小时过期
            String jsonResult = objectMapper.writeValueAsString(result);
            redisTemplate.opsForValue().set(CLASS_METHODS_CACHE_KEY, jsonResult, 2, TimeUnit.HOURS);

            return ApiResponse.success(result);

        } catch (Exception e) {
            log.error("获取 classMethod 列表异常", e);
            return ApiResponse.failure("获取列表失败");
        }
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
