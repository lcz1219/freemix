package com.freemix.freemix.enetiy;

import com.alibaba.fastjson2.JSONObject;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.beans.Transient;
import java.io.File;
import java.util.ArrayList;
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
    Date createTime;
    String level;
    List<String> tags;
    List<childGoals> childGoals;
    int planTime;
    int progress;
    String status;
    Integer statuslevel;
    Integer del;
    long deltime;
    String delDate;
    String richText;
    List fileList;
    Boolean isPublic;


    Boolean disRecover;
    boolean finish;
    List<JSONObject> collaborators;

    // Social features
    List<String> likedBy = new ArrayList<>();
    List<String> favoritedBy = new ArrayList<>();
    List<String> sharedBy = new ArrayList<>();
    
    // Share feature
    String shareToken;
    long sharedAt;
    
    // Comments will be stored in a separate collection, but we might want a count here or fetch them separately
}
