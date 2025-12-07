package com.freemix.freemix.service.impl;

import com.freemix.freemix.enetiy.UpdateLog;
import com.freemix.freemix.service.UpdateLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class UpdateLogServiceImpl implements UpdateLogService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Override
    public UpdateLog createUpdateLog(UpdateLog updateLog) {
        if (updateLog.getCreatedAt() == null) {
            updateLog.setCreatedAt(new Date());
        }
        if (updateLog.getUpdateTime() == null) {
            updateLog.setUpdateTime(new Date());
        }
        return mongoTemplate.save(updateLog);
    }

    @Override
    public List<UpdateLog> getAllUpdateLogs(int page, int size) {
        Query query = new Query();
        query.with(Sort.by(Sort.Direction.DESC, "updateTime"));
        query.skip((long) (page - 1) * size);
        query.limit(size);
        return mongoTemplate.find(query, UpdateLog.class);
    }

    @Override
    public UpdateLog getLatestUpdateLog() {
        Query query = new Query();
        query.addCriteria(Criteria.where("published").is(true));
        query.with(Sort.by(Sort.Direction.DESC, "version")); // Assuming semantic versioning or time sort
        query.limit(1);
        return mongoTemplate.findOne(query, UpdateLog.class);
    }

    @Override
    public void deleteUpdateLog(String id) {
        Query query = new Query();
        query.addCriteria(Criteria.where("id").is(id));
        mongoTemplate.remove(query, UpdateLog.class);
    }

    @Override
    public UpdateLog publishUpdateLog(String id) {
        UpdateLog log = mongoTemplate.findById(id, UpdateLog.class);
        if (log != null) {
            log.setPublished(true);
            log.setUpdateTime(new Date()); // Update time to publish time
            UpdateLog saved = mongoTemplate.save(log);
            
            // Push notification
            messagingTemplate.convertAndSend("/topic/updates", saved);
            
            return saved;
        }
        return null;
    }
}
