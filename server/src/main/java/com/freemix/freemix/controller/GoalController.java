package com.freemix.freemix.controller;

import com.alibaba.fastjson2.JSONObject;
import com.freemix.freemix.CheckToken;
import com.freemix.freemix.enetiy.Goal;
import com.freemix.freemix.util.ApiResponse;
import io.netty.util.internal.StringUtil;
import org.bson.json.JsonObject;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class GoalController extends BaseController {
@PostMapping("/editGoal")
@CheckToken
    public ApiResponse editGoal(@RequestBody String body){
        Goal goal = JSONObject.parseObject(body, Goal.class);
        Goal one = mongoTemplate.findOne(new Query(Criteria.where("title").is(goal.getTitle())
                .and("description").is(goal.getDescription()).and("Head").is(goal.getHead())), Goal.class);
        if(one != null){
            return ApiResponse.failure("已存在相同的目标");
        }
        if(StringUtil.isNullOrEmpty(goal.get_id())){
            goal.set_id(UUID.randomUUID().toString());

            mongoTemplate.insert(goal);
        }else {
            mongoTemplate.save(goal);
        }

        return ApiResponse.success(body);
    }
}
