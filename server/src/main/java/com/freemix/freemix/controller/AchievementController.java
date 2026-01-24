package com.freemix.freemix.controller;

import com.freemix.freemix.enetiy.Achievement;
import com.freemix.freemix.enetiy.User;
import com.freemix.freemix.enetiy.UserAchievement;
import com.freemix.freemix.service.AchievementService;
import com.freemix.freemix.util.ApiResponse;
import com.freemix.freemix.util.UserContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/achievements")
public class AchievementController {

    @Autowired
    private AchievementService achievementService;
    
    @Autowired
    private UserContextUtil userContextUtil;

    @GetMapping("/my")
    public ApiResponse getMyAchievements() {
        User currentUser = userContextUtil.getCurrentUser();
        if (currentUser == null) {
            return ApiResponse.failure("未登录");
        }
        String userId = currentUser.getUsername();
        
        List<Achievement> allAchievements = achievementService.getAllAchievements();
        List<UserAchievement> userAchievements = achievementService.getUserAchievements(userId);
        
        Map<String, UserAchievement> userMap = userAchievements.stream()
                .collect(Collectors.toMap(UserAchievement::getAchievementId, ua -> ua));
        
        // Merge data
        List<Map<String, Object>> result = allAchievements.stream().map(ach -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", ach.getId());
            map.put("title", ach.getTitle());
            map.put("description", ach.getDescription());
            map.put("icon", ach.getIcon());
            map.put("type", ach.getType());
            map.put("conditionValue", ach.getConditionValue());
            
            UserAchievement ua = userMap.get(ach.getId());
            if (ua != null) {
                map.put("unlocked", ua.isUnlocked());
                map.put("currentProgress", ua.getCurrentProgress());
                map.put("unlockedAt", ua.getUnlockedAt());
            } else {
                map.put("unlocked", false);
                map.put("currentProgress", 0);
                map.put("unlockedAt", null);
            }
            return map;
        }).collect(Collectors.toList());
        
        return ApiResponse.success(result);
    }
}
