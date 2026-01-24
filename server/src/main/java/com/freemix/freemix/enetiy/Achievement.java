package com.freemix.freemix.enetiy;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(value = "achievement")
@Data
public class Achievement {
    @Id
    String id;              // e.g., "night_owl"
    String title;           // "深夜战神"
    String description;     // "在凌晨..."
    String icon;            // "🦉"
    String type;            // "BEGINNER", "CUMULATIVE", "EASTER_EGG"
    Integer conditionValue; // e.g., 10
    String triggerEvent;    // "GOAL_FINISH", "LOGIN"
}
