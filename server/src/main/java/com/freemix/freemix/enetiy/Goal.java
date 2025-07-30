package com.freemix.freemix.enetiy;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(value = "goal")
@Data
public class Goal {
    String _id;
    String title;
    String description;
    String Head;
    Date endTime;
    String level;
    String classification;
    int planTime;


}
