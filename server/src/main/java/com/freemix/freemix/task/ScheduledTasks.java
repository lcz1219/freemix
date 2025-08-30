package com.freemix.freemix.task;

import com.freemix.freemix.enetiy.Goal;
import com.freemix.freemix.util.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class ScheduledTasks {
    @Autowired
    private MongoTemplate mongoTemplate;
    // 每 10 秒执行一次（无视上次任务状态）
    @Scheduled(fixedDelay = 60000)
    public void fixedRateTask() {
        List<Goal> goals = mongoTemplate.find(new Query(Criteria.where("del").ne(1)), Goal.class);

        goals.stream().forEach(goal -> {

            if(System.currentTimeMillis()>goal.getDeadline().getTime()&&!"expired".equals(goal.getStatus())){
                goal.setStatus("expired");
                mongoTemplate.save(goal);
                log.info("[ScheduledTasks] goal expired:{}", goal);



//           editGoal(goal.toString());
            }else{
                log.info("[ScheduledTasks] goal noexpired");
            }

        });

    }

    // 每天 14:30 执行

}