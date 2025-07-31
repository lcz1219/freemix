package com.freemix.freemix.controller;

import com.alibaba.fastjson2.JSONObject;
import com.freemix.freemix.CheckToken;
import com.freemix.freemix.enetiy.Goal;
import com.freemix.freemix.util.ApiResponse;
import io.netty.util.internal.StringUtil;
import org.bson.json.JsonObject;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class GoalController extends BaseController {
@PostMapping("/editGoal")
@CheckToken
    public ApiResponse editGoal(@RequestBody String body){
        Goal goal = JSONObject.parseObject(body, Goal.class);
        Goal one = mongoTemplate.findOne(new Query(Criteria.where("title").is(goal.getTitle())
                .and("description").is(goal.getDescription()).and("ower").is(goal.getOwner())), Goal.class);
        if(one != null){
            return ApiResponse.failure("已存在相同的目标");
        }
        if(StringUtil.isNullOrEmpty(goal.get_id())){
            goal.set_id(UUID.randomUUID().toString());
            goal.setProgress(0);
            goal.setStatus("in-progress");

            mongoTemplate.insert(goal);
        }else {
            mongoTemplate.save(goal);
        }

        return ApiResponse.success(body);
    }

@GetMapping("/getGoals/{ower}")
@CheckToken
    public ApiResponse getGoals(@PathVariable("ower") String ower){
    List<Goal> owner = mongoTemplate.find(new Query(Criteria.where("owner").is(ower)), Goal.class);
    return ApiResponse.success(owner);

}
}
