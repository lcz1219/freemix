package com.freemix.freemix.service.impl;

import com.freemix.freemix.enetiy.LoginLog;
import com.freemix.freemix.service.LoginLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class LoginLogServiceImpl implements LoginLogService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void saveLoginLog(LoginLog loginLog) {
        if (loginLog.getLoginTime() == null) {
            loginLog.setLoginTime(new Date());
        }
        mongoTemplate.insert(loginLog);
    }

    @Override
    public List<LoginLog> getLoginLogsByUserId(String userId, int page, int size) {
        Query query = new Query(Criteria.where("userId").is(userId))
                .with(Sort.by(Sort.Direction.DESC, "loginTime"))
                .skip((long) (page - 1) * size)
                .limit(size);
        return mongoTemplate.find(query, LoginLog.class);
    }

    @Override
    public List<LoginLog> getLoginLogsByDateRange(Date startDate, Date endDate, int page, int size) {
        Query query = new Query(Criteria.where("loginTime").gte(startDate).lte(endDate))
                .with(Sort.by(Sort.Direction.DESC, "loginTime"))
                .skip((long) (page - 1) * size)
                .limit(size);
        return mongoTemplate.find(query, LoginLog.class);
    }

    @Override
    public List<LoginLog> getRecentFailedLoginAttempts(String username, int count) {
        Query query = new Query(Criteria.where("username").is(username).and("loginSuccess").is(false))
                .with(Sort.by(Sort.Direction.DESC, "loginTime"))
                .limit(count);
        return mongoTemplate.find(query, LoginLog.class);
    }
}