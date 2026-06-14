package com.freemix.freemix.controller;

import com.alibaba.fastjson2.JSONObject;
import com.freemix.freemix.CheckToken;
import com.freemix.freemix.enetiy.AiMorning;
import com.freemix.freemix.util.ApiResponse;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Date;

@RestController
public class AiMorningController extends BaseController {

    @PostMapping("/getAiMorning")
    @CheckToken
    public ApiResponse getAiMorning() {
        String username = getCurrentUser().getUsername();
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

        Query query = new Query();
        query.addCriteria(Criteria.where("username").is(username));
        query.addCriteria(Criteria.where("date").is(today));

        AiMorning aiMorning = mongoTemplate.findOne(query, AiMorning.class);
        return ApiResponse.success(aiMorning, "查询成功");
    }

    @PostMapping("/saveAiMorning")
    @CheckToken
    public ApiResponse saveAiMorning(@RequestBody String body) {
        JSONObject json = JSONObject.parseObject(body);
        String content = json.getString("content");
        String username = getCurrentUser().getUsername();
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

        Query query = new Query();
        query.addCriteria(Criteria.where("username").is(username));
        query.addCriteria(Criteria.where("date").is(today));

        AiMorning existing = mongoTemplate.findOne(query, AiMorning.class);
        if (existing != null) {
            return ApiResponse.failure("今天已经生成过晨报了");
        }

        AiMorning aiMorning = new AiMorning();
        aiMorning.setUsername(username);
        aiMorning.setContent(content);
        aiMorning.setDate(today);
        aiMorning.setCreatedAt(new Date());

        mongoTemplate.save(aiMorning);

        return ApiResponse.success(aiMorning, "保存成功");
    }
}