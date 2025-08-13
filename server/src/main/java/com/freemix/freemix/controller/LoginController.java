package com.freemix.freemix.controller;

import com.alibaba.fastjson2.JSONObject;
import com.freemix.freemix.CheckToken;
import com.freemix.freemix.enetiy.User;
import com.freemix.freemix.util.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController("")
public class LoginController {
    @Autowired
    MongoTemplate mongoTemplate;
    @Autowired
    RedisTemplate redisTemplate;
    @PostMapping("/register")
    public ApiResponse register(@RequestBody  String body){
        User user = JSONObject.parseObject(body, User.class);
        User existUser = mongoTemplate.findOne(new Query().addCriteria(Criteria.where("name").is(user.getUsername())), User.class);
        if(existUser != null){
            return ApiResponse.failure("此用户名已存在");
        }
        mongoTemplate.insert(user);
        return ApiResponse.success(user);

    }@PostMapping("/login")

    public ApiResponse login(@RequestBody  String body){
        User user = JSONObject.parseObject(body, User.class);
        User username = mongoTemplate.findOne(new Query().addCriteria(Criteria.where("username").is(user.getUsername())
                .and("password").is(user.getPassword())), User.class);
        if(username == null){
            return ApiResponse.failure("登录失败");
        }
        user.setToken(UUID.randomUUID().toString());
        redisTemplate.opsForValue().set("token", user.getToken(),60, TimeUnit.MINUTES);
        return ApiResponse.success(user);

    }
    @GetMapping("/getOwerList")
    @CheckToken
    public ApiResponse getOwerList(){
        List<User> users = mongoTemplate.find(new Query(), User.class);
        Set<JSONObject> collect = users.stream().map(e -> {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("value", e.getUsername());
            jsonObject.put("text", e.getUsername());
            return jsonObject;
        }).collect(Collectors.toSet());

        return ApiResponse.success(collect);
    }
}


