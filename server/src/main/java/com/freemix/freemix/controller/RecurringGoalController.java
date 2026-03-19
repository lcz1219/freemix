package com.freemix.freemix.controller;

import com.alibaba.fastjson2.JSONObject;
import com.freemix.freemix.CheckToken;
import com.freemix.freemix.enetiy.RecurringGoal;
import com.freemix.freemix.enetiy.User;
import com.freemix.freemix.util.ApiResponse;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import org.springframework.scheduling.support.CronExpression;
import java.time.ZonedDateTime;
import java.time.ZoneId;

@Slf4j
@RestController
public class RecurringGoalController extends BaseController {

    @GetMapping("/getRecurringGoals")
    @CheckToken
    public ApiResponse getRecurringGoals() {
        User currentUser = userContextUtil.getCurrentUser();
        if (currentUser == null) {
            return ApiResponse.failure("用户未登录");
        }
        
        Query query = new Query(Criteria.where("owner").is(currentUser.getUsername()).and("del").ne(1));
        List<RecurringGoal> list = mongoTemplate.find(query, RecurringGoal.class);
        
        return ApiResponse.success(list);
    }

    @PostMapping("/editRecurringGoal")
    @CheckToken
    public ApiResponse editRecurringGoal(@RequestBody String body) {
        RecurringGoal rule = JSONObject.parseObject(body, RecurringGoal.class);
        User currentUser = userContextUtil.getCurrentUser();
        
        if (currentUser == null) {
            return ApiResponse.failure("用户未登录");
        }

        // 校验 Cron 表达式
        if ("cron".equals(rule.getRecurrenceType())) {
            if (StringUtil.isNullOrEmpty(rule.getCronExpression())) {
                return ApiResponse.failure("Cron 表达式不能为空");
            }
            if (!CronExpression.isValidExpression(rule.getCronExpression())) {
                return ApiResponse.failure("无效的 Cron 表达式: " + rule.getCronExpression());
            }
        }
        
        if (StringUtil.isNullOrEmpty(rule.get_id())) {
            rule.set_id(UUID.randomUUID().toString());
            rule.setOwner(currentUser.getUsername());
            rule.setCreateTime(new Date());
            rule.setDel(0);
            rule.setIsActive(true);
            
            // 设置首次执行时间
            rule.setNextExecutionTime(calculateInitialExecutionTime(rule));
            
            mongoTemplate.insert(rule);
            return ApiResponse.success(rule, "创建成功");
        } else {
            RecurringGoal existing = mongoTemplate.findById(rule.get_id(), RecurringGoal.class);
            if (existing == null || !existing.getOwner().equals(currentUser.getUsername())) {
                return ApiResponse.failure("规则不存在或无权操作");
            }
            
            // 如果修改了循环类型或 Cron 表达式，重新计算下次执行时间
            boolean typeChanged = !existing.getRecurrenceType().equals(rule.getRecurrenceType());
            boolean cronChanged = "cron".equals(rule.getRecurrenceType()) && !rule.getCronExpression().equals(existing.getCronExpression());
            
            if (typeChanged || cronChanged) {
                rule.setNextExecutionTime(calculateInitialExecutionTime(rule));
            } else {
                rule.setNextExecutionTime(existing.getNextExecutionTime());
            }
            
            rule.setOwner(currentUser.getUsername());
            rule.setLastGeneratedTime(existing.getLastGeneratedTime());
            
            mongoTemplate.save(rule);
            return ApiResponse.success(rule, "保存成功");
        }
    }

    @PostMapping("/deleteRecurringGoal")
    @CheckToken
    public ApiResponse deleteRecurringGoal(@RequestParam String id) {
        User currentUser = userContextUtil.getCurrentUser();
        RecurringGoal existing = mongoTemplate.findById(id, RecurringGoal.class);
        
        if (existing == null || !existing.getOwner().equals(currentUser.getUsername())) {
            return ApiResponse.failure("规则不存在或无权操作");
        }
        
        existing.setDel(1);
        mongoTemplate.save(existing);
        return ApiResponse.success(null, "已删除");
    }

    @PostMapping("/toggleRecurringGoal")
    @CheckToken
    public ApiResponse toggleRecurringGoal(@RequestParam String id, @RequestParam Boolean active) {
        User currentUser = userContextUtil.getCurrentUser();
        RecurringGoal existing = mongoTemplate.findById(id, RecurringGoal.class);
        
        if (existing == null || !existing.getOwner().equals(currentUser.getUsername())) {
            return ApiResponse.failure("规则不存在或无权操作");
        }
        
        existing.setIsActive(active);
        mongoTemplate.save(existing);
        return ApiResponse.success(null, active ? "已启用" : "已停用");
    }

    @GetMapping("/previewCron")
    @CheckToken
    public ApiResponse previewCron(@RequestParam String cron) {
        if (!CronExpression.isValidExpression(cron)) {
            return ApiResponse.failure("无效的 Cron 表达式");
        }
        
        try {
            CronExpression expression = CronExpression.parse(cron);
            ZonedDateTime next = ZonedDateTime.now();
            List<Date> nextExecutions = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                next = expression.next(next);
                if (next == null) break;
                nextExecutions.add(Date.from(next.toInstant()));
            }
            return ApiResponse.success(nextExecutions);
        } catch (Exception e) {
            return ApiResponse.failure("解析失败: " + e.getMessage());
        }
    }
///
    private Date calculateInitialExecutionTime(RecurringGoal rule) {
        String type = rule.getRecurrenceType();
        
        if ("cron".equals(type) && rule.getCronExpression() != null) {
            try {
                CronExpression cron = CronExpression.parse(rule.getCronExpression());
                ZonedDateTime next = cron.next(ZonedDateTime.now());
                return Date.from(next.toInstant());
            } catch (Exception e) {
                log.error("Cron parsing failed", e);
            }
        }

        Calendar cal = Calendar.getInstance();
        // 默认从明天凌晨开始执行，防止今天重复生成
        cal.add(Calendar.DAY_OF_YEAR, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        
        if ("weekly".equals(type)) {
            // 每周一
            cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            if (cal.getTime().before(new Date())) {
                cal.add(Calendar.WEEK_OF_YEAR, 1);
            }
        } else if ("monthly".equals(type)) {
            // 每月1号
            cal.set(Calendar.DAY_OF_MONTH, 1);
            if (cal.getTime().before(new Date())) {
                cal.add(Calendar.MONTH, 1);
            }
        }
        return cal.getTime();
    }
}
