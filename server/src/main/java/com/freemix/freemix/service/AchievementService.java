package com.freemix.freemix.service;

import com.freemix.freemix.enetiy.Achievement;
import com.freemix.freemix.enetiy.UserAchievement;
import java.util.List;

public interface AchievementService {
    /**
     * Initialize achievements in the database
     */
    void initAchievements();

    /**
     * Check if any achievements are unlocked based on the event
     * @param userId The user ID
     * @param triggerEvent The event type (e.g., "GOAL_FINISH")
     * @param context Additional context object (e.g., the Goal object)
     * @return List of newly unlocked achievements
     */
    List<Achievement> checkAndUnlock(String userId, String triggerEvent, Object context);

    /**
     * Get all achievements for a user (including locked ones with progress)
     */
    List<UserAchievement> getUserAchievements(String userId);

    /**
     * Get all defined achievements
     */
    List<Achievement> getAllAchievements();
}
