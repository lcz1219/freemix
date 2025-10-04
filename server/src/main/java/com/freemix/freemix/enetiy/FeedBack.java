package com.freemix.freemix.enetiy;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(value = "feedback")
@Data
public class FeedBack {
    String _id;
    String type;
    String subject;
    String content;
    String feedResult;
    long feedResultTime;
    String status;
    long feedTime;
    long createdAt;
    String contact;

}
