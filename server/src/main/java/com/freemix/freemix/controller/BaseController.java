package com.freemix.freemix.controller;

import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.freemix.freemix.enetiy.Relation;
import com.freemix.freemix.enetiy.User;
import com.freemix.freemix.util.EnvironmentChecker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
public class BaseController {
    @Autowired
    MongoTemplate mongoTemplate;
    @Autowired
    EnvironmentChecker environmentChecker;

    public boolean editCollaborator(String goalId, List<String> collaborator,String role) {
        if("owner".equals(role)){
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
     void delRelationPerson(String username, String goalId) {
        Query query=new Query();
        query.addCriteria(Criteria.where("username").is(username));
        query.addCriteria(Criteria.where("goalId").is(goalId));
        List<Relation> ones = mongoTemplate.find(query, Relation.class);
        ones.forEach(relation -> {
            if(relation!=null){
                relation.setDel(1);
                mongoTemplate.save(relation);
            }
        });

    }

    public List<JSONObject> genCollaborator(String goalId) {

        List<Relation> strings = mongoTemplate.find(Query.query(Criteria.where("goalId").is(goalId).and("del").ne(1)), Relation.class);
        List<JSONObject> users = new ArrayList<>();
        strings.forEach(relation -> {
            User username1 = mongoTemplate.findOne(Query.query(Criteria.where("username").is(relation.getUsername()).and("del").ne(1)), User.class);
            // 2. 创建 ObjectMapper 实例
            ObjectMapper objectMapper = new ObjectMapper();

            // 方法A: 先转成JSON字符串，再包装为JSONObject (常用)
            String jsonString = null;
            try {
                jsonString = objectMapper.writeValueAsString(username1);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            JSONObject jsonObject = JSONObject.parseObject(jsonString);
            jsonObject.put("role", relation.getRole());

            if(username1 != null) {
                users.add(jsonObject);
            }
        });

return users;
    }

}
