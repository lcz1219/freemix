package com.freemix.freemix.enetiy;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Document(collection = "AiMorning")
public class AiMorning {
    @Id
    private String id;
    private String username;
    private String content;
    private String date; // Format: yyyy-MM-dd
    private Date createdAt;
}