package com.freemix.freemix.task;

import com.freemix.freemix.enetiy.Goal;
import com.freemix.freemix.enetiy.RecurringGoal;
import com.freemix.freemix.service.AchievementService;
import com.freemix.freemix.util.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class ScheduledTasks {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private AchievementService achievementService;

    // 每 1 分钟执行一次，处理过期目标
    @Scheduled(fixedDelay = 60000)
    public void fixedRateTask() {
        List<Goal> goals = mongoTemplate.find(new Query(Criteria.where("del").ne(1)), Goal.class);

        goals.stream().forEach(goal -> {

            if(System.currentTimeMillis()>goal.getDeadline().getTime()&&"in-progress".equals(goal.getStatus())) {
                goal.setStatus("expired");
                mongoTemplate.save(goal);
                log.info("[ScheduledTasks] goal expired:{}", goal);



//           editGoal(goal.toString());
            }else{
                log.info("[ScheduledTasks] goal noexpired");
            }

        });
    }

    // 处理循环目标自动生成
    @Scheduled(fixedDelay = 60000)
    public void generateRecurringGoals() {
        Date now = new Date();
        // 查找活跃且未被逻辑删除，且下次执行时间小于等于现在的循环规则
        Query query = new Query(Criteria.where("isActive").is(true)
                .and("del").ne(1)
                .and("nextExecutionTime").lte(now));
        
        List<RecurringGoal> recurringGoals = mongoTemplate.find(query, RecurringGoal.class);
        
        for (RecurringGoal rule : recurringGoals) {
            try {
                // 生成新目标
                Goal newGoal = new Goal();
                newGoal.set_id(UUID.randomUUID().toString());
                newGoal.setTitle(rule.getTitle());
                newGoal.setDescription(rule.getDescription());
                newGoal.setOwner(rule.getOwner());
                newGoal.setTags(rule.getTags());
                newGoal.setLevel(rule.getLevel());
                newGoal.setIsPublic(rule.getIsPublic());
                newGoal.setRichText(rule.getRichText());
                newGoal.setChildGoals(rule.getChildGoals());
                newGoal.setCreateTime(new Date());
                newGoal.setStatus("in-progress");
                newGoal.setProgress(0);
                
                // 设置截止日期 (默认该周期的结束时间)
                newGoal.setDeadline(calculateDeadline(rule, rule.getNextExecutionTime()));
                
                mongoTemplate.insert(newGoal);
                log.info("[ScheduledTasks] Generated new goal from recurring rule: {} for user: {}", rule.getTitle(), rule.getOwner());
                
                // 触发成就检查
                try {
                    achievementService.checkAndUnlock(newGoal.getOwner(), "GOAL_CREATE", newGoal);
                } catch (Exception e) {
                    log.error("[ScheduledTasks] Failed to check achievements for generated goal", e);
                }
                
                // 更新规则状态
                rule.setLastGeneratedTime(now);
                rule.setNextExecutionTime(calculateNextExecutionTime(rule, rule.getNextExecutionTime()));
                mongoTemplate.save(rule);
                
            } catch (Exception e) {
                log.error("[ScheduledTasks] Failed to generate goal for rule: " + rule.get_id(), e);
            }
        }
    }

    private Date calculateDeadline(RecurringGoal rule, Date startTime) {
        String type = rule.getRecurrenceType();
        Calendar cal = Calendar.getInstance();
        cal.setTime(startTime);
        
        if ("cron".equals(type) && rule.getCronExpression() != null) {
            // Cron 类型的截止日期默认为下一次执行时间
            return calculateNextExecutionTime(rule, startTime);
        }
        
        if ("daily".equals(type)) {
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
        } else if ("weekly".equals(type)) {
            cal.add(Calendar.DAY_OF_YEAR, 6);
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
        } else if ("monthly".equals(type)) {
            cal.add(Calendar.MONTH, 1);
            cal.add(Calendar.DAY_OF_YEAR, -1);
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
        }
        return cal.getTime();
    }

    private Date calculateNextExecutionTime(RecurringGoal rule, Date currentTime) {
        String type = rule.getRecurrenceType();
        if ("cron".equals(type) && rule.getCronExpression() != null) {
            try {
                CronExpression cron = CronExpression.parse(rule.getCronExpression());
                ZonedDateTime zdt = ZonedDateTime.ofInstant(currentTime.toInstant(), ZoneId.systemDefault());
                ZonedDateTime next = cron.next(zdt);
                return Date.from(next.toInstant());
            } catch (Exception e) {
                log.error("Invalid cron expression: " + rule.getCronExpression(), e);
                // 兜底回退到每日执行
                Calendar cal = Calendar.getInstance();
                cal.setTime(currentTime);
                cal.add(Calendar.DAY_OF_YEAR, 1);
                return cal.getTime();
            }
        }
        
        Calendar cal = Calendar.getInstance();
        cal.setTime(currentTime);
        if ("daily".equals(type)) {
            cal.add(Calendar.DAY_OF_YEAR, 1);
        } else if ("weekly".equals(type)) {
            cal.add(Calendar.WEEK_OF_YEAR, 1);
        } else if ("monthly".equals(type)) {
            cal.add(Calendar.MONTH, 1);
        }
        return cal.getTime();
    }
}