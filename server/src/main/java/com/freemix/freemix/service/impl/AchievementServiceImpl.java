package com.freemix.freemix.service.impl;

import com.freemix.freemix.enetiy.Achievement;
import com.freemix.freemix.enetiy.Goal;
import com.freemix.freemix.enetiy.LoginLog;
import com.freemix.freemix.enetiy.UserAchievement;
import com.freemix.freemix.service.AchievementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.*;

@Slf4j
@Service
public class AchievementServiceImpl implements AchievementService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @PostConstruct
    public void initAchievements() {
        List<Achievement> defaults = new ArrayList<>();
        defaults.add(createAchievement("first_step", "初次立誓", "创建你的第一个目标", "🌱", "BEGINNER", 1, "GOAL_CREATE"));
        defaults.add(createAchievement("first_win", "首战告捷", "完成你的第一个目标", "🏆", "BEGINNER", 1, "GOAL_FINISH"));
        defaults.add(createAchievement("goal_hunter", "目标猎人", "累计完成 10 个目标", "🏹", "CUMULATIVE", 10, "GOAL_FINISH"));
        defaults.add(createAchievement("goal_master", "成就大师", "累计完成 100 个目标", "👑", "CUMULATIVE", 100, "GOAL_FINISH"));
        defaults.add(createAchievement("social_butterfly", "并肩作战", "首次邀请协作者参与目标", "🤝", "SOCIAL", 1, "ADD_COLLABORATOR"));
        defaults.add(createAchievement("night_owl", "深夜战神", "在凌晨 2 点到 5 点之间完成任务", "🦉", "EASTER_EGG", 1, "GOAL_FINISH"));
        defaults.add(createAchievement("weekend_warrior", "周末战士", "在周六或周日完成任务", "🏖️", "EASTER_EGG", 1, "GOAL_FINISH"));
        defaults.add(createAchievement("consistency", "持之以恒", "连续登录 3 天", "🔥", "CUMULATIVE", 3, "LOGIN"));
        defaults.add(createAchievement("perfectionist", "完美主义", "完成一个包含至少 5 个子目标的目标", "✨", "DIFFICULT", 1, "GOAL_FINISH"));

        for (Achievement ach : defaults) {
            if (!mongoTemplate.exists(new Query(Criteria.where("_id").is(ach.getId())), Achievement.class)) {
                mongoTemplate.save(ach);
            }
        }
    }

    private Achievement createAchievement(String id, String title, String desc, String icon, String type, Integer val, String event) {
        Achievement a = new Achievement();
        a.setId(id);
        a.setTitle(title);
        a.setDescription(desc);
        a.setIcon(icon);
        a.setType(type);
        a.setConditionValue(val);
        a.setTriggerEvent(event);
        return a;
    }

    @Override
    public List<Achievement> checkAndUnlock(String userId, String triggerEvent, Object context) {
        List<Achievement> unlockedAchievements = new ArrayList<>();
        if (userId == null) return unlockedAchievements;
        
        List<Achievement> achievements = mongoTemplate.find(new Query(Criteria.where("triggerEvent").is(triggerEvent)), Achievement.class);
        
        for (Achievement ach : achievements) {
            // Check if already unlocked
            UserAchievement ua = mongoTemplate.findOne(new Query(Criteria.where("userId").is(userId).and("achievementId").is(ach.getId())), UserAchievement.class);
            if (ua != null && ua.isUnlocked() && !"CUMULATIVE".equals(ach.getType())) {
                continue; // Non-cumulative already unlocked, skip
            }
            if (ua == null) {
                ua = new UserAchievement();
                ua.setId(UUID.randomUUID().toString());
                ua.setUserId(userId);
                ua.setAchievementId(ach.getId());
                if("LOGIN".equals(triggerEvent)){

                    ua.setCurrentProgress(1);
                }else{

                    ua.setCurrentProgress(0);
                }
                ua.setUnlocked(false);
            }

            boolean unlocked = false;
            int progress = ua.getCurrentProgress();

            switch (ach.getId()) {
                case "first_step": // GOAL_CREATE
                    long count = mongoTemplate.count(new Query(Criteria.where("owner").is(userId)), Goal.class);
                    progress = (int) count;
                    unlocked = count >= 1;
                    break;
                case "first_win": // GOAL_FINISH
                case "goal_hunter":
                case "goal_master":
                    long completedCount = mongoTemplate.count(new Query(Criteria.where("owner").is(userId).and("status").is("completed")), Goal.class);
                    progress = (int) completedCount;
                    unlocked = completedCount >= ach.getConditionValue();
                    break;
                case "social_butterfly": // ADD_COLLABORATOR
                    unlocked = true;
                    progress = 1;
                    break;
                case "night_owl": // GOAL_FINISH
                    Calendar now = Calendar.getInstance();
                    int hour = now.get(Calendar.HOUR_OF_DAY);
                    if (hour >= 2 && hour < 5) {
                        unlocked = true;
                        progress = 1;
                    }
                    break;
                case "weekend_warrior": // GOAL_FINISH
                    Calendar today = Calendar.getInstance();
                    int dayOfWeek = today.get(Calendar.DAY_OF_WEEK);
                    if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
                        unlocked = true;
                        progress = 1;
                    }
                    break;
                case "consistency": // LOGIN
                    // Check last 3 days logins
                    progress = checkConsecutiveLogins(userId, 3);; // Simplified progress
                    break;
                case "perfectionist": // GOAL_FINISH
                    if (context instanceof Goal) {
                        Goal g = (Goal) context;
                        if (g.getChildGoals() != null && g.getChildGoals().size() >= 5) {
                            unlocked = true;
                            progress = 1;
                        }
                    }
                    break;
            }

            if (unlocked && !ua.isUnlocked()&&!"consistency".equals(ach.getId())) {
                ua.setUnlocked(true);
                ua.setUnlockedAt(new Date());
                ua.setCurrentProgress(ach.getConditionValue()); // Cap progress
                mongoTemplate.save(ua);
                unlockedAchievements.add(ach);
                log.info("User {} unlocked achievement {}", userId, ach.getTitle());
            } else if (!ua.isUnlocked()) {
                 // Update progress only
                ua.setCurrentProgress(progress);
                mongoTemplate.save(ua);
            }
        }
        return unlockedAchievements;
    }

    private Integer checkConsecutiveLogins(String userId, int days) {
        Query query = new Query(Criteria.where("username").is(userId));
        query.with(Sort.by(Sort.Direction.DESC, "loginTime"));
        query.limit(20);
        List<LoginLog> logs = mongoTemplate.find(query, LoginLog.class);
        
        if (logs.isEmpty()) return 0;
        
        Set<String> loginDates = new HashSet<>();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
        for (LoginLog log : logs) {
            if (log.getLoginTime() != null) {
                loginDates.add(sdf.format(log.getLoginTime()));
            }
        }
        
        Calendar cal = Calendar.getInstance();
        int consecutive = 0;
        for (int i = 0; i < days; i++) {
            String dateStr = sdf.format(cal.getTime());
            if (loginDates.contains(dateStr)) {
                consecutive++;
            } else {
                // If today is not found, maybe they logged in yesterday? 
                // But the logic is "consecutive 3 days". 
                // If today is missing, we check if yesterday is present.
                // But simplified: check if TODAY, YESTERDAY, DAY_BEFORE are all present.
                // If user logs in today, this function runs. So today should be present.
                break;
            }
            cal.add(Calendar.DAY_OF_MONTH, -1);
        }
        
        return consecutive;
    }

    @Override
    public List<UserAchievement> getUserAchievements(String userId) {
        return mongoTemplate.find(new Query(Criteria.where("userId").is(userId)), UserAchievement.class);
    }

    @Override
    public List<Achievement> getAllAchievements() {
        return mongoTemplate.findAll(Achievement.class);
    }
}
