package com.freemix.freemix.enetiy;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;

@Document(value = "user_achievement")
@Data
public class UserAchievement {
    @Id
    String id;
    String userId;          // Linked to User.id
    String achievementId;   // Linked to Achievement.id
    Integer currentProgress;// Current progress (e.g., 3/10)
    boolean unlocked;       // Is unlocked
    Date unlockedAt;        // Unlock timestamp
}
