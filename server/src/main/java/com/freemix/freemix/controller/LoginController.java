package com.freemix.freemix.controller;

import com.alibaba.fastjson2.JSONObject;
import com.freemix.freemix.CheckToken;
import com.freemix.freemix.enetiy.User;
import com.freemix.freemix.util.ApiResponse;
import com.freemix.freemix.util.CaptchaUtil;
import io.lettuce.core.json.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

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

    }
    
    @PostMapping("/login")
    public ApiResponse login(@RequestBody  String body){
        JSONObject jsonObject = JSONObject.parseObject(body);
        String username = jsonObject.getString("username");
        String password = jsonObject.getString("password");
        String captcha = jsonObject.getString("captcha");
        
        // 验证验证码
        String captchaKey = "captcha_" + username;
        String storedCaptcha = (String) redisTemplate.opsForValue().get(captchaKey);
        if (storedCaptcha == null || !storedCaptcha.equalsIgnoreCase(captcha)) {
            return ApiResponse.failure("验证码错误");
        }
        
        // 验证成功后删除验证码
        redisTemplate.delete(captchaKey);
        
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        
        User usernameFromDB = mongoTemplate.findOne(new Query().addCriteria(Criteria.where("username").is(user.getUsername())
                .and("password").is(user.getPassword())), User.class);
        if(usernameFromDB == null){
            return ApiResponse.failure("登录失败");
        }
        user.setToken(UUID.randomUUID().toString());
        redisTemplate.opsForValue().set("token", user.getToken(),60, TimeUnit.MINUTES);
        return ApiResponse.success(user);
    }
    
    @PostMapping("/captcha")
    public ApiResponse getCaptcha(@RequestBody String body) {
        // 生成验证码
        CaptchaUtil.CaptchaResult captchaResult = CaptchaUtil.generateCaptcha();
        // 存储验证码到Redis，设置5分钟过期
        JSONObject jsonObject = JSONObject.parseObject(body);
        String username = jsonObject.getString("username");
        String captchaKey = "captcha_" + username;
        redisTemplate.opsForValue().set(captchaKey, captchaResult.getCode(), 5, TimeUnit.MINUTES);
        
        JSONObject result = new JSONObject();
        result.put("image", "data:image/png;base64," + captchaResult.getImageBase64());
        result.put("captchaId", captchaKey);
        
        return ApiResponse.success(result);
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