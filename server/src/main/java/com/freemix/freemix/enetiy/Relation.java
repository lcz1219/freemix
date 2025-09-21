package com.freemix.freemix.enetiy;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(value = "relation")
@Data
public class Relation {
    String _id;
    String goalId;
    String username;
    Date addTime;
    String role;
    Integer del;

}
