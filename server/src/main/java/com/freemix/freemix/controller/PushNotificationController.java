package com.freemix.freemix.controller;

import com.alibaba.fastjson2.JSONObject;
import com.freemix.freemix.CheckToken;
import com.freemix.freemix.enetiy.PushNotification;
import com.freemix.freemix.util.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 推送通知控制器
 * 处理 iOS 原生通知的存储、查询和已读标记
 */
@Slf4j
@RestController
public class PushNotificationController extends BaseController {

    /**
     * 保存一条推送通知（由前端JS在收到原生事件后调用）
     */
    @PostMapping("/saveNotification")
    @CheckToken
    public ApiResponse saveNotification(@RequestBody String body) {
        log.info("PushNotificationController: saveNotification");
        JSONObject json = JSONObject.parseObject(body);
        String username = getCurrentUser().getUsername();

        PushNotification notification = new PushNotification();
        notification.setUsername(username);
        notification.setTitle(json.getString("title"));
        notification.setBody(json.getString("body"));
        notification.setType(json.getString("type"));
        notification.setGoalId(json.getString("goalId"));
        notification.setGoalTitle(json.getString("goalTitle"));
        if (json.containsKey("createdAt")) {
            notification.setCreatedAt(json.getLong("createdAt"));
        }

        mongoTemplate.save(notification);
        return ApiResponse.success(notification, "保存成功");
    }

    /**
     * 批量保存推送通知（冷启动时同步多条）
     */
    @PostMapping("/saveNotifications")
    @CheckToken
    public ApiResponse saveNotifications(@RequestBody String body) {
        log.info("PushNotificationController: saveNotifications");

        JSONObject json = JSONObject.parseObject(body);
        String username = getCurrentUser().getUsername();
        List<com.alibaba.fastjson2.JSONArray> list = json.getJSONArray("list").toList(com.alibaba.fastjson2.JSONArray.class);

        for (Object item : json.getJSONArray("list")) {
            JSONObject obj = (JSONObject) item;
            PushNotification notification = new PushNotification();
            notification.setUsername(username);
            notification.setTitle(obj.getString("title"));
            notification.setBody(obj.getString("body"));
            notification.setType(obj.getString("type"));
            notification.setGoalId(obj.getString("goalId"));
            notification.setGoalTitle(obj.getString("goalTitle"));
            if (obj.containsKey("createdAt")) {
                notification.setCreatedAt(obj.getLong("createdAt"));
            }
            mongoTemplate.save(notification);
        }

        return ApiResponse.success(null, "保存成功");
    }

    /**
     * 获取用户的所有通知（按时间倒序）
     */
    @PostMapping("/getNotifications")
    @CheckToken
    public ApiResponse getNotifications() {
        log.info("PushNotificationController: getNotifications");

        String username = getCurrentUser().getUsername();

        Query query = new Query();
        query.addCriteria(Criteria.where("username").is(username));
        query.with(Sort.by(Sort.Direction.DESC, "createdAt"));

        List<PushNotification> list = mongoTemplate.find(query, PushNotification.class);
        return ApiResponse.success(list, "查询成功");
    }

    /**
     * 获取未读通知数量
     */
    @PostMapping("/getUnreadNotificationCount")
    @CheckToken
    public ApiResponse getUnreadNotificationCount() {
        log.info("PushNotificationController: getUnreadNotificationCount");

        String username = getCurrentUser().getUsername();

        Query query = new Query();
        query.addCriteria(Criteria.where("username").is(username));
        query.addCriteria(Criteria.where("isRead").is(false));

        long count = mongoTemplate.count(query, PushNotification.class);
        return ApiResponse.success(count, "查询成功");
    }

    /**
     * 标记单条通知为已读
     */
    @PostMapping("/markNotificationRead")
    @CheckToken
    public ApiResponse markNotificationRead(@RequestBody String body) {
        log.info("PushNotificationController: markNotificationRead");

        JSONObject json = JSONObject.parseObject(body);
        String id = json.getString("id");

        Query query = new Query();
        query.addCriteria(Criteria.where("id").is(id));

        Update update = new Update();
        update.set("isRead", true);

        mongoTemplate.updateFirst(query, update, PushNotification.class);
        return ApiResponse.success(null, "已标记为已读");
    }

    /**
     * 标记所有通知为已读
     */
    @PostMapping("/markAllNotificationsRead")
    @CheckToken
    public ApiResponse markAllNotificationsRead() {
        log.info("PushNotificationController: markAllNotificationsRead");

        String username = getCurrentUser().getUsername();

        Query query = new Query();
        query.addCriteria(Criteria.where("username").is(username));
        query.addCriteria(Criteria.where("isRead").is(false));

        Update update = new Update();
        update.set("isRead", true);

        mongoTemplate.updateMulti(query, update, PushNotification.class);
        return ApiResponse.success(null, "全部已标记为已读");
    }

    /**
     * 删除单条通知
     */
    @PostMapping("/deleteNotification")
    @CheckToken
    public ApiResponse deleteNotification(@RequestBody String body) {
        log.info("PushNotificationController: deleteNotification");

        JSONObject json = JSONObject.parseObject(body);
        String id = json.getString("id");

        Query query = new Query();
        query.addCriteria(Criteria.where("id").is(id));

        mongoTemplate.remove(query, PushNotification.class);
        return ApiResponse.success(null, "删除成功");
    }

    /**
     * 清空所有通知
     */
    @PostMapping("/clearNotifications")
    @CheckToken
    public ApiResponse clearNotifications() {
        log.info("PushNotificationController: clearNotifications");

        String username = getCurrentUser().getUsername();

        Query query = new Query();
        query.addCriteria(Criteria.where("username").is(username));

        mongoTemplate.remove(query, PushNotification.class);
        return ApiResponse.success(null, "已清空");
    }
}
