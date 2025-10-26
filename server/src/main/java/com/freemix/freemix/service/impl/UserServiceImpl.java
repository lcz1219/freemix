package com.freemix.freemix.service.impl;

import com.freemix.freemix.enetiy.User;
import com.freemix.freemix.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public User findByGithubId(String githubId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("githubId").is(githubId));
        return mongoTemplate.findOne(query, User.class);
    }

    @Override
    public User save(User user) {
        return mongoTemplate.save(user);
    }

    public User findByToken(String token) {
        Query query = new Query();
        query.addCriteria(Criteria.where("token").is(token));
        return mongoTemplate.findOne(query, User.class);
    }
}