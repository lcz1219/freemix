package com.freemix.freemix.controller;

import com.alibaba.fastjson2.JSONObject;
import com.freemix.freemix.CheckToken;
import com.freemix.freemix.enetiy.FeedBack;
import com.freemix.freemix.enetiy.FeedStatus;
import com.freemix.freemix.enetiy.User;
import com.freemix.freemix.util.ApiResponse;
import com.freemix.freemix.util.UserContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
public class FeedBackController extends BaseController {
@Autowired
    UserContextUtil userContextUtil;
    @PostMapping("addFeedback")
    @CheckToken
    public ApiResponse addFeedback(@RequestBody String body) {
        log.info("addFeedback : {}", body);
        User currentUser = userContextUtil.getCurrentUser();

        FeedBack feedBack = JSONObject.parseObject(body, FeedBack.class);
        feedBack.setCreatedBy(currentUser.getUsername());
        feedBack.set_id(UUID.randomUUID().toString());
        feedBack.setCreatedAt( System.currentTimeMillis());
        mongoTemplate.insert(feedBack);
        return ApiResponse.success(feedBack);
    }

    @GetMapping("findFeedBack")
    // @CheckToken
    public ApiResponse findFeedBack() {
        List<FeedBack> feedBacks = mongoTemplate.find(new Query(), FeedBack.class);
        return ApiResponse.success(feedBacks);
    }

    @PostMapping("replyFeedback")
    @CheckToken
    public ApiResponse replyFeedback(@RequestBody String body) {
        log.info("replyFeedback : {}", body);
        JSONObject jsonObject = JSONObject.parseObject(body);
        String _id = jsonObject.getString("feedbackId");
        String replyContent = jsonObject.getString("replyContent");
        FeedBack feedBack = mongoTemplate.findOne(new Query().addCriteria(Criteria.where("_id").is(_id)), FeedBack.class);
        if(feedBack != null) {

        feedBack.setFeedResult(replyContent);
        feedBack.setFeedResultTime( System.currentTimeMillis());
        feedBack.setStatus(FeedStatus.resolved);
        mongoTemplate.save(feedBack);
        return ApiResponse.success(feedBack);
        }else{
            return ApiResponse.failure("此反馈已不存在，若需要请联系管理员");
        }

    }
}
