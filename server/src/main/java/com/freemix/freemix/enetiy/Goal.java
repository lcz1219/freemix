package com.freemix.freemix.enetiy;

import lombok.Builder;
import lombok.Data;
import org.json.JSONObject;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Document(value = "goal")
@Data
public class Goal {
    String _id;
    String title;
    String description;
    String owner;
    Date deadline;
    String level;
    List<String> tags;
    List<childGoals> childGoals;
    int planTime;
    int progress;
    String status;
    Integer del;
    long deltime;
    Boolean disRecover;
    boolean finish;


}
