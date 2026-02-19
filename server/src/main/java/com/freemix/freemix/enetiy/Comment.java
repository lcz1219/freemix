package com.freemix.freemix.enetiy;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(value = "comment")
@Data
public class Comment {
    String _id;
    String goalId;
    String userId;
    String username;
    String userAvatar; // Optional
    String content;
    Date createTime;
    
    // Reply support (optional for now)
    String parentId;
    String replyToUser;
}
