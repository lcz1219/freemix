package com.freemix.freemix.controller;

import com.alibaba.fastjson2.JSONObject;
import com.freemix.freemix.CheckToken;
import com.freemix.freemix.enetiy.AIMessage;
import com.freemix.freemix.enetiy.User;
import com.freemix.freemix.service.AIMessageService;
import com.freemix.freemix.util.ApiResponse;
import org.apache.poi.ss.formula.functions.T;
import org.bson.BsonArray;
import org.bson.Document;
import com.mongodb.client.AggregateIterable;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AI消息控制器
 * 处理AI助手对话记录的保存和查询
 */
@RestController
@RequestMapping("/ai-messages")
@CrossOrigin(origins = "*") // 允许跨域请求
public class AIMessageController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(AIMessageController.class);
    
    @Autowired
    private AIMessageService aiMessageService;
    
    /**
     * 获取指定用户的会话列表（通过聚合获取唯一 sessionId）
     */
    @GetMapping("/sessions/{username}")
    @CheckToken
    public ApiResponse<List<JSONObject>> getSessions(@PathVariable String username) {
        log.info("获取用户 {} 的会话列表", username);
        return aiMessageService.getUserSessions(username);
    }

    /**
     * 获取指定会话的历史消息记录
     */
    @GetMapping("/session/{sessionId}")
    @CheckToken
    public ApiResponse<List<AIMessage>> getSessionHistory(@PathVariable String sessionId) {
        log.info("获取会话 {} 的历史消息", sessionId);
        return aiMessageService.getSessionMessages(sessionId);
    }

    /**
     * 删除指定的会话及其所有消息
     */
    @GetMapping("/delsession/{sessionId}")
    @CheckToken
    public ApiResponse<String> deleteSession(@PathVariable String sessionId) {
        log.info("删除会话及其所有消息: {}", sessionId);
        return aiMessageService.deleteSession(sessionId);
    }

    /**
     * 保存AI消息
     * @param body AI消息JSON
     * @return ApiResponse 包含保存结果的响应
     */
    @PostMapping("/save")
    @CheckToken
    public ApiResponse<AIMessage> saveAIMessage(@RequestBody String body) {
        AIMessage aiMessage = JSONObject.parseObject(body, AIMessage.class);
        log.info("保存AI消息: 用户 = {}, 会话 = {}, 问题 = {}", aiMessage.getUsername(), aiMessage.getSessionId(), aiMessage.getUserQuestion());
        return aiMessageService.saveAIMessage(aiMessage);
    }
    String insights="insights";
    @PostMapping("/saveInsights")
    @CheckToken
    public ApiResponse<T> saveInsights(@RequestBody String body) {

        JSONObject jsonObject = JSONObject.parseObject(body);
        mongoTemplate.insert(jsonObject,insights);
        return ApiResponse.success();

    }
    @PostMapping("/getInsights")
    @CheckToken
    public ApiResponse getInsights() {

        User currentUser = getCurrentUser();
       long earlyest = System.currentTimeMillis()-3600000;

        Query query = new Query(Criteria.where("username")
                .is(currentUser.getUsername()
                ).and("createdAt").gte(earlyest)
        );
        List<JSONObject> jsonObjects = mongoTemplate.find(query, JSONObject.class, insights);

        return ApiResponse.success(jsonObjects);

    }
    
    /**
     * 获取当前用户的所有AI消息记录
     * @return ApiResponse 包含消息列表的响应
     */
    @GetMapping("/all")
    @CheckToken
    public ApiResponse<List<AIMessage>> getAllAIMessages() {
        log.info("获取用户所有AI消息记录");
        return aiMessageService.getAllAIMessages();
    }
    
    /**
     * 获取当前用户的最近N条AI消息记录
     * @param limit 限制条数
     * @return ApiResponse 包含消息列表的响应
     */
    @GetMapping("/recent")
    @CheckToken
    public ApiResponse<List<AIMessage>> getRecentAIMessages(@RequestParam(defaultValue = "50") int limit) {
        log.info("获取用户最近 {} 条AI消息记录", limit);
        return aiMessageService.getRecentAIMessages(limit);
    }
    
    /**
     * 获取当前用户在指定日期范围的AI消息记录
     * @param startTime 开始时间戳（毫秒）
     * @param endTime 结束时间戳（毫秒）
     * @return ApiResponse 包含消息列表的响应
     */
    @GetMapping("/by-date-range")
    @CheckToken
    public ApiResponse<List<AIMessage>> getAIMessagesByDateRange(
            @RequestParam long startTime,
            @RequestParam long endTime) {
        log.info("获取用户在指定时间范围的AI消息记录: {} - {}", startTime, endTime);
        return aiMessageService.getAIMessagesByDateRange(startTime, endTime);
    }
    
    /**
     * 删除指定的AI消息记录
     * @param messageId 消息ID
     * @return ApiResponse 包含操作结果的响应
     */
    @DeleteMapping("/delete/{messageId}")
    @CheckToken
    public ApiResponse<String> deleteAIMessage(@PathVariable String messageId) {
        log.info("删除AI消息记录: {}", messageId);
        return aiMessageService.deleteAIMessage(messageId);
    }
    
    /**
     * 清空当前用户的所有AI消息记录
     * @return ApiResponse 包含操作结果的响应
     */
    @DeleteMapping("/clear-all")
    @CheckToken
    public ApiResponse<String> clearAllAIMessages() {
        log.info("清空用户所有AI消息记录");
        return aiMessageService.clearAllAIMessages();
    }
    
    /**
     * 获取当前用户的AI消息总数
     * @return ApiResponse 包含消息总数的响应
     */
    @GetMapping("/count")
    @CheckToken
    public ApiResponse<Long> getAIMessageCount() {
        log.info("获取用户AI消息总数");
        return aiMessageService.getAIMessageCount();
    }
    
    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 执行由 AI 生成的 MQL (MongoDB 聚合管道)
     * @param body 包含 pipeline (JSON 字符串) 和 collection (可选，默认 goal)
     * @return ApiResponse 包含查询结果列表
     */
    @PostMapping("/query-mql")
    @CheckToken
    public ApiResponse executeMql(@RequestBody JSONObject body) {
        User currentUser = getCurrentUser();
        // 权限校验：仅管理员可进行统计分析
        // if (currentUser == null || !"1033519224@qq.com".equals(currentUser.getEmail())) {
        //     return ApiResponse.failure("您没有权限执行数据统计分析", 403);
        // }

        try {
            String pipelineJson = body.getString("pipeline");
            String collectionName = body.getString("collection"); // 默认为 "goal"
            String question = body.getString("question"); // 默认为 "goal"

            if (collectionName == null || collectionName.isEmpty()) {
                collectionName = "goal";
            }
            log.info("pipelineJson: {}",pipelineJson);

            String cleanedMql = pipelineJson
                    .replaceAll("ISODate\\(\"([^\"]+)\"\\)", "{\"\\$date\": \"$1\"}")
                    .replaceAll("new Date\\(\"([^\"]+)\"\\)", "{\"\\$date\": \"$1\"}");

            // 处理算术表达式 (例如 1000 * 60 * 60 * 24)
            cleanedMql = evaluateArithmetic(cleanedMql);

            // 将 JSON 数组解析为 MongoDB Pipeline
//            List<Document> pipeline = com.alibaba.fastjson2.JSON.parseArray(cleanedMql, Document.class);
            log.info("问题内容:{} 执行 MQL 统计, 集合: {}, cleanedMql: {}",question,  collectionName, cleanedMql);

            List<Document> pipeline = BsonArray.parse(cleanedMql).stream()
                    .map(v -> Document.parse(v.asDocument().toJson()))
                    .collect(Collectors.toList());
            // 执行聚合查询
            AggregateIterable<Document> results = mongoTemplate.getCollection(collectionName).aggregate(pipeline);
            
            List<Document> output = new ArrayList<>();
            results.forEach(output::add);
            
            return ApiResponse.success(output);
        } catch (Exception e) {
            log.error("MQL 执行失败", e);
            return ApiResponse.failure("查询语法错误或数据库异常: " + e.getMessage());
        }
    }

    /**
     * 获取指定用户的历史AI消息记录
     * @param username 用户名
     * @return ApiResponse 包含消息列表的响应
     */
    @GetMapping("/{username}/history")
    @CheckToken
    public ApiResponse<List<AIMessage>> getUserHistory(@PathVariable String username) {
        log.info("获取用户 {} 的历史AI消息记录", username);
        return aiMessageService.getUserHistory(username);
    }

    /**
     * 简单的算术表达式评估器，专门用于处理 JSON 中的数字乘法 (如 1000 * 60 * 60)
     */
    private String evaluateArithmetic(String input) {
        if (input == null || !input.contains("*")) return input;
        
        // 匹配数字之间的乘号，支持多级乘法
        // 匹配模式：数字、空格、星号、空格、数字...
        Pattern pattern = Pattern.compile("(\\d+(?:\\s*\\*\\s*\\d+)+)");
        Matcher matcher = pattern.matcher(input);
        StringBuffer sb = new StringBuffer();
        
        while (matcher.find()) {
            String expression = matcher.group(1);
            try {
                long result = 1;
                String[] parts = expression.split("\\*");
                for (String part : parts) {
                    result *= Long.parseLong(part.trim());
                }
                matcher.appendReplacement(sb, String.valueOf(result));
            } catch (Exception e) {
                // 如果解析失败，保留原样
                matcher.appendReplacement(sb, Matcher.quoteReplacement(expression));
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}