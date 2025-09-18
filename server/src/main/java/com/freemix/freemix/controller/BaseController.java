package com.freemix.freemix.controller;

import com.alibaba.fastjson2.JSONObject;
import com.freemix.freemix.enetiy.Relation;
import com.freemix.freemix.enetiy.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Controller
public class BaseController {
    @Autowired
    MongoTemplate mongoTemplate;

    public boolean editCollaborator(String goalId, List<String> collaborator,String role) {
        if("creater".equals(role)){
            Query query = new Query();
            query.addCriteria(Criteria.where("goalId").is(goalId));
            query.addCriteria(Criteria.where("role").is(role));
            Relation one = mongoTemplate.findOne(query, Relation.class);
            if(one != null) {
                return true;
            }
        }
        collaborator.forEach(username -> {
            Relation relation = new Relation();
           relation.setGoalId(goalId);
           relation.setUsername(username);
           relation.setAddTime(new Date());
           relation.setRole(role);
           mongoTemplate.insert(relation);



        });
        return true;

    }

}
