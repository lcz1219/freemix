package com.freemix.freemix.enetiy;

import com.alibaba.fastjson2.JSONObject;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.beans.Transient;
import java.io.File;
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
    Integer statuslevel;
    Integer del;
    long deltime;
    List fileList;

    Boolean disRecover;
    boolean finish;
    List<JSONObject> collaborators;



}
