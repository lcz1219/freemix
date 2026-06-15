package com.freemix.freemix.controller;

import com.alibaba.fastjson2.JSONObject;
import com.freemix.freemix.CheckToken;
import com.freemix.freemix.enetiy.*;
import com.freemix.freemix.util.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 数据导出控制器
 * 将用户的所有数据导出为 JSON 文件，支持跨设备迁移和备份
 */
@RestController
public class DataExportController extends BaseController {

    /**
     * 导出用户全部数据（返回 JSON 字符串，由前端触发下载）
     */
    @PostMapping("/exportUserData")
    @CheckToken
    public ApiResponse exportUserData() {
        String username = getCurrentUser().getUsername();
        JSONObject exportData = new JSONObject();

        // 1. 用户信息（排除敏感字段）
        User user = mongoTemplate.findOne(
                new Query(Criteria.where("username").is(username)), User.class);
        if (user != null) {
            JSONObject userJson = new JSONObject();
            userJson.put("username", user.getUsername());
            userJson.put("chinesename", user.getChinesename());
            userJson.put("email", user.getEmail());
            userJson.put("avatarUrl", user.getAvatarUrl());
            userJson.put("fashionTitle", user.getFashionTitle());
            userJson.put("githubId", user.getGithubId());
            userJson.put("qqOpenId", user.getQqOpenId());
            exportData.put("profile", userJson);
        }

        // 2. 所有目标（含子目标、协作人）
        List<Goal> goals = mongoTemplate.find(
                new Query(Criteria.where("owner").is(username)), Goal.class);
        exportData.put("goals", goals);

        // 3. 协作关系
        List<Relation> relations = mongoTemplate.find(
                new Query(Criteria.where("username").is(username)), Relation.class);
        exportData.put("relations", relations);

        // 4. AI 对话记录
        List<AIMessage> aiMessages = mongoTemplate.find(
                new Query(Criteria.where("username").is(username))
                        .with(Sort.by(Sort.Direction.ASC, "createdAt")),
                AIMessage.class);
        exportData.put("aiMessages", aiMessages);

        // 5. AI 晨报记录
        List<AiMorning> aiMornings = mongoTemplate.find(
                new Query(Criteria.where("username").is(username))
                        .with(Sort.by(Sort.Direction.DESC, "createdAt")),
                AiMorning.class);
        exportData.put("aiMornings", aiMornings);

        // 6. 推送通知
        List<PushNotification> notifications = mongoTemplate.find(
                new Query(Criteria.where("username").is(username))
                        .with(Sort.by(Sort.Direction.DESC, "createdAt")),
                PushNotification.class);
        exportData.put("notifications", notifications);

        // 7. 循环目标
        List<RecurringGoal> recurringGoals = mongoTemplate.find(
                new Query(Criteria.where("owner").is(username)), RecurringGoal.class);
        exportData.put("recurringGoals", recurringGoals);

        // 8. 成就
        List<UserAchievement> achievements = mongoTemplate.find(
                new Query(Criteria.where("userId").is(username)), UserAchievement.class);
        exportData.put("achievements", achievements);

        // 9. 登录日志
        List<LoginLog> loginLogs = mongoTemplate.find(
                new Query(Criteria.where("username").is(username))
                        .with(Sort.by(Sort.Direction.DESC, "loginTime")),
                LoginLog.class);
        exportData.put("loginLogs", loginLogs);

        // 10. 好友关系
        List<Friendship> friendships = mongoTemplate.find(
                new Query(new Criteria().orOperator(
                        Criteria.where("fromUser").is(username),
                        Criteria.where("toUser").is(username)
                )), Friendship.class);
        exportData.put("friendships", friendships);

        return ApiResponse.success(exportData, "导出成功");
    }

    /**
     * 导出用户数据并直接以文件流形式下载（可直接浏览器访问/下载）
     */
    @PostMapping("/downloadUserData")
    @CheckToken
    public void downloadUserData(HttpServletResponse response) {
        String username = getCurrentUser().getUsername();

        // 复用 exportUserData 逻辑
        JSONObject exportData = new JSONObject();

        // （复用上面相同的数据组装逻辑）
        User user = mongoTemplate.findOne(
                new Query(Criteria.where("username").is(username)), User.class);
        if (user != null) {
            JSONObject userJson = new JSONObject();
            userJson.put("username", user.getUsername());
            userJson.put("chinesename", user.getChinesename());
            userJson.put("email", user.getEmail());
            userJson.put("avatarUrl", user.getAvatarUrl());
            userJson.put("fashionTitle", user.getFashionTitle());
            userJson.put("githubId", user.getGithubId());
            userJson.put("qqOpenId", user.getQqOpenId());
            exportData.put("profile", userJson);
        }

        exportData.put("goals", mongoTemplate.find(
                new Query(Criteria.where("owner").is(username)), Goal.class));
        exportData.put("relations", mongoTemplate.find(
                new Query(Criteria.where("username").is(username)), Relation.class));
        exportData.put("aiMessages", mongoTemplate.find(
                new Query(Criteria.where("username").is(username))
                        .with(Sort.by(Sort.Direction.ASC, "createdAt")), AIMessage.class));
        exportData.put("aiMornings", mongoTemplate.find(
                new Query(Criteria.where("username").is(username))
                        .with(Sort.by(Sort.Direction.DESC, "createdAt")), AiMorning.class));
        exportData.put("notifications", mongoTemplate.find(
                new Query(Criteria.where("username").is(username))
                        .with(Sort.by(Sort.Direction.DESC, "createdAt")), PushNotification.class));
        exportData.put("recurringGoals", mongoTemplate.find(
                new Query(Criteria.where("owner").is(username)), RecurringGoal.class));
        exportData.put("achievements", mongoTemplate.find(
                new Query(Criteria.where("userId").is(username)), UserAchievement.class));
        exportData.put("loginLogs", mongoTemplate.find(
                new Query(Criteria.where("username").is(username))
                        .with(Sort.by(Sort.Direction.DESC, "loginTime")), LoginLog.class));
        exportData.put("friendships", mongoTemplate.find(
                new Query(new Criteria().orOperator(
                        Criteria.where("fromUser").is(username),
                        Criteria.where("toUser").is(username)
                )), Friendship.class));

        // JSON 美化输出
        String jsonStr = exportData.toJSONString();
        // 美化
        jsonStr = com.alibaba.fastjson2.JSON.toJSONString(
                com.alibaba.fastjson2.JSON.parseObject(jsonStr),
                com.alibaba.fastjson2.JSONWriter.Feature.PrettyFormat);

        try {
            String fileName = "freemix-backup-" + username + "-"
                    + new java.text.SimpleDateFormat("yyyyMMdd-HHmmss").format(new java.util.Date()) + ".json";

            response.setContentType("application/json;charset=UTF-8");
            response.setHeader("Content-Disposition",
                    "attachment; filename=\"" + java.net.URLEncoder.encode(fileName, "UTF-8") + "\"");
            response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");

            OutputStream os = response.getOutputStream();
            os.write(jsonStr.getBytes(StandardCharsets.UTF_8));
            os.flush();
            os.close();
        } catch (Exception e) {
//            log.error("导出数据失败", e);
        }
    }
}
