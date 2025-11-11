package com.freemix.freemix.controller;

import com.alibaba.fastjson.JSONObject;
import com.freemix.freemix.CheckToken;
import com.freemix.freemix.enetiy.AIGen;
import com.freemix.freemix.util.ApiResponse;
import com.freemix.freemix.util.UserContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * AI生成记录控制器
 * 提供AI目标生成的记录管理、分享和查询功能
 */
@Slf4j
@RestController
@RequestMapping("/api/aiGen")
public class AIGenController extends BaseController {
    
    @Autowired
    private UserContextUtil userContextUtil;
    
    /**
     * 保存AI生成记录
     * @param request 保存请求
     * @return 操作结果
     */
    @PostMapping("/save")
    @CheckToken
    public ApiResponse saveAIGen(@RequestBody String body) {
        try {
            JSONObject json = JSONObject.parseObject(body);
            String username = json.getString("username");
            SaveAIGenRequest request = JSONObject.parseObject(json.getString("saveData"), SaveAIGenRequest.class);

            AIGen aiGen = new AIGen();
            
            // 设置基本信息
            aiGen.setUsername(username);
            aiGen.setUserInput(request.getUserInput());
            aiGen.setAiResponse(request.getAiResponse());
            aiGen.setGoalTitle(request.getGoalTitle());
            aiGen.setChildGoals(request.getChildGoals());
            aiGen.setChatMessages(request.getChatMessages());
            
            // 保存到数据库
            mongoTemplate.insert(aiGen);
            
            return ApiResponse.success("保存成功");
        } catch (Exception e) {
            log.error("保存AI生成记录失败", e);
            return ApiResponse.failure("保存失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户的AI生成记录列表
     * @param page 页码
     * @param size 每页数量
     * @param status 状态筛选（可选）
     * @return 记录列表
     */
    @GetMapping("/list")
    @CheckToken
    public ApiResponse getAIGenList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam String username) {
        try {
            
            Query query = new Query();
            query.addCriteria(Criteria.where("username").is(username));
            
            // 如果指定了状态筛选
            if (status != null && !status.trim().isEmpty()) {
                query.addCriteria(Criteria.where("status").is(status));
            }
            
            // 按创建时间倒序排列
            query.with(org.springframework.data.domain.Sort.by(
                org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));
            
            // 分页
            Pageable pageable = PageRequest.of(page, size);
            query.with(pageable);
            
            List<AIGen> aiGens = mongoTemplate.find(query, AIGen.class);
            
            // 计算总数量
            long total = mongoTemplate.count(
                Query.query(Criteria.where("username").is(username)), AIGen.class);
            
            return ApiResponse.success( new AIGenListResponse(aiGens, total, page, size),"查询成功");
        } catch (Exception e) {
            log.error("获取AI生成记录列表失败", e);
            return ApiResponse.failure("查询失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取单个AI生成记录详情
     * @param id 记录ID
     * @return 记录详情
     */
    @GetMapping("/detail/{id}")
    @CheckToken
    public ApiResponse getAIGenDetail(@PathVariable String id, @RequestParam String username) {
        try {
            
            Query query = new Query();
            query.addCriteria(Criteria.where("_id").is(id));
            query.addCriteria(Criteria.where("username").is(username));
            
            AIGen aiGen = mongoTemplate.findOne(query, AIGen.class);
            if (aiGen == null) {
                return ApiResponse.failure("记录不存在或无权访问");
            }
            
            return ApiResponse.success(aiGen,"查询成功" );
        } catch (Exception e) {
            log.error("获取AI生成记录详情失败", e);
            return ApiResponse.failure("查询失败: " + e.getMessage());
        }
    }
    
    /**
     * 确认AI生成记录
     * @param id 记录ID
     * @return 操作结果
     */
    @PostMapping("/confirm/{id}")
    @CheckToken
    public ApiResponse confirmAIGen(@PathVariable String id, @RequestParam String username) {
        try {
            
            Query query = new Query();
            query.addCriteria(Criteria.where("_id").is(id));
            query.addCriteria(Criteria.where("username").is(username));
            
            AIGen aiGen = mongoTemplate.findOne(query, AIGen.class);
            if (aiGen == null) {
                return ApiResponse.failure("记录不存在或无权访问");
            }
            
            // 检查是否已过期
            if (aiGen.isExpired()) {
                return ApiResponse.failure("记录已过期，无法确认");
            }
            
            // 更新状态
            Update update = new Update();
            update.set("status", "confirmed");
            update.set("confirmedAt", System.currentTimeMillis());
            
            mongoTemplate.updateFirst(query, update, AIGen.class);
            
            return ApiResponse.success("确认成功");
        } catch (Exception e) {
            log.error("确认AI生成记录失败", e);
            return ApiResponse.failure("确认失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除AI生成记录
     * @param id 记录ID
     * @return 操作结果
     */
    @DeleteMapping("/delete/{id}")
    @CheckToken
    public ApiResponse deleteAIGen(@PathVariable String id, @RequestParam String username) {
        try {
            
            Query query = new Query();
            query.addCriteria(Criteria.where("_id").is(id));
            query.addCriteria(Criteria.where("username").is(username));
            
            AIGen aiGen = mongoTemplate.findOne(query, AIGen.class);
            if (aiGen == null) {
                return ApiResponse.failure("记录不存在或无权访问");
            }
            
            mongoTemplate.remove(query, AIGen.class);
            
            return ApiResponse.success("删除成功");
        } catch (Exception e) {
            log.error("删除AI生成记录失败", e);
            return ApiResponse.failure("删除失败: " + e.getMessage());
        }
    }
    
    /**
     * 生成分享链接
     * @param id 记录ID
     * @return 分享信息
     */
    @PostMapping("/share/{id}")
    @CheckToken
    public ApiResponse shareAIGen(@PathVariable String id, @RequestParam String username) {
        try {
            
            Query query = new Query();
            query.addCriteria(Criteria.where("_id").is(id));
            query.addCriteria(Criteria.where("username").is(username));
            
            AIGen aiGen = mongoTemplate.findOne(query, AIGen.class);
            if (aiGen == null) {
                return ApiResponse.failure("记录不存在或无权访问");
            }
            
            // 如果已经有分享token，直接返回
            if (aiGen.getShareToken() != null && !aiGen.getShareToken().trim().isEmpty()) {
                return ApiResponse.success(new ShareResponse(aiGen.getShareToken()),"获取分享链接成功");
            }
            
            // 生成新的分享token
            aiGen.generateShareToken();
            
            Update update = new Update();
            update.set("shareToken", aiGen.getShareToken());
            update.set("shareStatus", "shared");
            update.set("sharedAt", aiGen.getSharedAt());
            
            mongoTemplate.updateFirst(query, update, AIGen.class);
            
            return ApiResponse.success( new ShareResponse(aiGen.getShareToken()),"生成分享链接成功");
        } catch (Exception e) {
            log.error("生成分享链接失败", e);
            return ApiResponse.failure("分享失败: " + e.getMessage());
        }
    }
    
    /**
     * 通过分享token获取记录
     * @param token 分享token
     * @return 记录信息（公开部分）
     */
    @GetMapping("/share/{token}")
    public ApiResponse getSharedAIGen(@PathVariable String token) {
        try {
            Query query = new Query();
            query.addCriteria(Criteria.where("shareToken").is(token));
            query.addCriteria(Criteria.where("shareStatus").is("shared"));
            
            AIGen aiGen = mongoTemplate.findOne(query, AIGen.class);
            if (aiGen == null) {
                return ApiResponse.failure("分享链接不存在或已失效");
            }
            
            // 返回公开信息（隐藏敏感信息）
            SharedAIGenResponse response = new SharedAIGenResponse();
            response.setId(aiGen.getId());
            response.setGoalTitle(aiGen.getGoalTitle());
            response.setUserInput(aiGen.getUserInput());
            response.setAiResponse(aiGen.getAiResponse());
            response.setChildGoals(aiGen.getChildGoals());
            response.setCreatedAt(aiGen.getCreatedAt());
            
            return ApiResponse.success(response,"获取分享内容成功" );
        } catch (Exception e) {
            log.error("通过分享token获取记录失败", e);
            return ApiResponse.failure("获取失败: " + e.getMessage());
        }
    }
    
    /**
     * 清理过期的AI生成记录
     * @return 清理结果
     */
    @PostMapping("/cleanup")
    @CheckToken
    public ApiResponse cleanupExpired() {
        try {
            Query query = new Query();
            query.addCriteria(Criteria.where("expireAt").lt(System.currentTimeMillis()));
            
            List<AIGen> expiredRecords = mongoTemplate.find(query, AIGen.class);
            
            // 删除过期记录
            mongoTemplate.remove(query, AIGen.class);
            
            log.info("清理了 {} 条过期的AI生成记录", expiredRecords.size());
            
            return ApiResponse.success("清理完成，已删除 " + expiredRecords.size() + " 条过期记录");
        } catch (Exception e) {
            log.error("清理过期记录失败", e);
            return ApiResponse.failure("清理失败: " + e.getMessage());
        }
    }
    
    // 内部请求/响应类
    public static class SaveAIGenRequest {
        private String userInput;
        private String aiResponse;
        private String goalTitle;
        private List<AIGen.SubGoal> childGoals;
        private List<AIGen.ChatMessage> chatMessages;
        
        // Getters and Setters
        public String getUserInput() { return userInput; }
        public void setUserInput(String userInput) { this.userInput = userInput; }
        public String getAiResponse() { return aiResponse; }
        public void setAiResponse(String aiResponse) { this.aiResponse = aiResponse; }
        public String getGoalTitle() { return goalTitle; }
        public void setGoalTitle(String goalTitle) { this.goalTitle = goalTitle; }
        public List<AIGen.SubGoal> getChildGoals() { return childGoals; }
        public void setChildGoals(List<AIGen.SubGoal> childGoals) { this.childGoals = childGoals; }
        public List<AIGen.ChatMessage> getChatMessages() { return chatMessages; }
        public void setChatMessages(List<AIGen.ChatMessage> chatMessages) { this.chatMessages = chatMessages; }
    }
    
    public static class AIGenListResponse {
        private List<AIGen> records;
        private long total;
        private int page;
        private int size;
        
        public AIGenListResponse(List<AIGen> records, long total, int page, int size) {
            this.records = records;
            this.total = total;
            this.page = page;
            this.size = size;
        }
        
        // Getters and Setters
        public List<AIGen> getRecords() { return records; }
        public void setRecords(List<AIGen> records) { this.records = records; }
        public long getTotal() { return total; }
        public void setTotal(long total) { this.total = total; }
        public int getPage() { return page; }
        public void setPage(int page) { this.page = page; }
        public int getSize() { return size; }
        public void setSize(int size) { this.size = size; }
    }
    
    public static class ShareResponse {
        private String shareToken;
        private String shareUrl;
        
        public ShareResponse(String shareToken) {
            this.shareToken = shareToken;
            this.shareUrl = "/share/" + shareToken; // 前端需要拼接完整URL
        }
        
        // Getters and Setters
        public String getShareToken() { return shareToken; }
        public void setShareToken(String shareToken) { this.shareToken = shareToken; }
        public String getShareUrl() { return shareUrl; }
        public void setShareUrl(String shareUrl) { this.shareUrl = shareUrl; }
    }
    
    public static class SharedAIGenResponse {
        private String id;
        private String goalTitle;
        private String userInput;
        private String aiResponse;
        private List<AIGen.SubGoal> childGoals;
        private long createdAt;
        
        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getGoalTitle() { return goalTitle; }
        public void setGoalTitle(String goalTitle) { this.goalTitle = goalTitle; }
        public String getUserInput() { return userInput; }
        public void setUserInput(String userInput) { this.userInput = userInput; }
        public String getAiResponse() { return aiResponse; }
        public void setAiResponse(String aiResponse) { this.aiResponse = aiResponse; }
        public List<AIGen.SubGoal> getChildGoals() { return childGoals; }
        public void setChildGoals(List<AIGen.SubGoal> childGoals) { this.childGoals = childGoals; }
        public long getCreatedAt() { return createdAt; }
        public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    }
}