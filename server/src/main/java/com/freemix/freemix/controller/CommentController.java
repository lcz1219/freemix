package com.freemix.freemix.controller;

import com.alibaba.fastjson2.JSONObject;
import com.freemix.freemix.CheckToken;
import com.freemix.freemix.enetiy.Comment;
import com.freemix.freemix.util.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
public class CommentController extends BaseController {

    @PostMapping("/addComment")
    @CheckToken
    public ApiResponse addComment(@RequestBody String body) {
        Comment comment = JSONObject.parseObject(body, Comment.class);
        if (comment.getGoalId() == null || comment.getContent() == null) {
            return ApiResponse.failure("Invalid comment data");
        }
        
        comment.set_id(UUID.randomUUID().toString());
        comment.setCreateTime(new Date());
        
        mongoTemplate.insert(comment);
        return ApiResponse.success(comment);
    }

    @GetMapping("/getComments/{goalId}")
    @CheckToken
    public ApiResponse getComments(@PathVariable("goalId") String goalId) {
        Query query = new Query(Criteria.where("goalId").is(goalId));
        List<Comment> comments = mongoTemplate.find(query, Comment.class);
        
        comments.sort((c1, c2) -> {
            if (c1.getCreateTime() == null) return -1;
            if (c2.getCreateTime() == null) return 1;
            return c1.getCreateTime().compareTo(c2.getCreateTime());
        });
        
        return ApiResponse.success(comments);
    }
}
