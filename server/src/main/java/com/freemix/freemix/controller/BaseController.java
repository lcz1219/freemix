package com.freemix.freemix.controller;

import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.freemix.freemix.enetiy.Goal;
import com.freemix.freemix.enetiy.Relation;
import com.freemix.freemix.enetiy.User;
import com.freemix.freemix.enetiy.childGoals;
import com.freemix.freemix.util.EnvironmentChecker;
import com.freemix.freemix.util.UserContextUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class BaseController {
    @Autowired
    MongoTemplate mongoTemplate;
    @Autowired
    EnvironmentChecker environmentChecker;
    @Autowired
    HttpServletRequest request;
    @Autowired
    UserContextUtil userContextUtil;

    public boolean editCollaborator(String goalId, List<String> collaborator,String role) {
        if("owner".equals(role)){
            Query query = new Query();
            query.addCriteria(Criteria.where("goalId").is(goalId));
            query.addCriteria(Criteria.where("role").is(role));
            Relation one = mongoTemplate.findOne(query, Relation.class);
            if(one != null) {
                return true;
            }
        }
        collaborator.forEach(username -> {
            Relation relation = new Relation();
           relation.setGoalId(goalId);
           relation.setUsername(username);
           relation.setAddTime(new Date());
           relation.setRole(role);
           mongoTemplate.insert(relation);



        });
        return true;

    }
     void delRelationPerson(String username, String goalId) {
        Query query=new Query();
        query.addCriteria(Criteria.where("username").is(username));
        query.addCriteria(Criteria.where("goalId").is(goalId));
        List<Relation> ones = mongoTemplate.find(query, Relation.class);
        ones.forEach(relation -> {
            if(relation!=null){
                relation.setDel(1);
                mongoTemplate.save(relation);
            }
        });

    }
    public static void sortChildGoals(Goal goal) {
        //排序子目标

        // 获取目标下的所有子目标，并按特定规则进行排序：
// 1. 未完成的子目标（isFinish = false）排在前面
// 2. 已完成的子目标（isFinish = true）排在后面，并按完成日期降序排列（最新完成的排在最前）

// 首先，筛选出所有未完成的子目标
        List<childGoals> noFinChildGoals = goal.getChildGoals()
                .stream()
                // 过滤条件：只保留未完成的目标（isFinish() 返回 false）
                .filter(e -> !e.isFinish())
                // 收集为 List
                .collect(Collectors.toList());

// 然后，筛选出所有已完成的子目标，并按完成日期排序
        List<childGoals> dateChildGoals = goal.getChildGoals()
                .stream()
                // 过滤条件：只保留已完成的目标（isFinish() 返回 true）
                .filter(e -> e.isFinish())
                // 对已完成的目标进行排序：
                .sorted(Comparator.comparing(
                        // 主排序键：完成日期
                        childGoals::getFinishDate,
                        // 处理完成日期可能为 null 的情况：
                        // 1. Comparator.nullsFirst - 规定 null 值应被视为小于任何非 null 值
                        // 2. Comparator.naturalOrder() - 对于非 null 值，使用其自然顺序（即日期从早到晚）
                        // 组合效果：null 日期排在最前，非 null 日期按升序排列（从早到晚）
                        Comparator.nullsFirst(Comparator.naturalOrder())
                        // 反转排序顺序：将自然顺序（升序）变为降序，使得：
                        // 1. 非 null 日期中，最新日期排在最前
                        // 2. null 日期仍然排在最前（因为 null 被视为最小，反转后最小变最大？不对！注意：nullsFirst 的语义不受 reversed 影响）
                        // 注意：这里需要特别小心，因为 reversed() 会反转整个比较器的逻辑
                        // 实际上，这里的 reversed() 会使：
                        // - 非 null 日期：从升序（早到晚）变为降序（晚到早）
                        // - null 值：由于 nullsFirst 的定义，null 仍然会被视为最小，但反转后，最小会变成最大吗？
                        // 实际上，Comparator.nullsFirst 的语义是：null 总是小于非 null。这个语义不会因为 reversed() 而改变。
                        // 因此，反转后的效果是：null 值仍然排在最前面，而非 null 日期按降序排列（最新的在最前）
                ).reversed())
                .collect(Collectors.toList());

// 将两部分列表合并：未完成的在前，已完成的（按日期降序）在后
        noFinChildGoals.addAll(dateChildGoals);

// 将排序后的列表设置回目标对象
        goal.setChildGoals(noFinChildGoals);
    }
    public List<JSONObject> genCollaborator(String goalId) {

        List<Relation> strings = mongoTemplate.find(Query.query(Criteria.where("goalId").is(goalId).and("del").ne(1)), Relation.class);
        List<JSONObject> users = new ArrayList<>();
        strings.forEach(relation -> {
            User username1 = mongoTemplate.findOne(Query.query(Criteria.where("username").is(relation.getUsername()).and("del").ne(1)), User.class);
            // 2. 创建 ObjectMapper 实例
            ObjectMapper objectMapper = new ObjectMapper();

            // 方法A: 先转成JSON字符串，再包装为JSONObject (常用)
            String jsonString = null;
            try {
                jsonString = objectMapper.writeValueAsString(username1);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            JSONObject jsonObject = JSONObject.parseObject(jsonString);
            jsonObject.put("role", relation.getRole());

            if(username1 != null) {
                users.add(jsonObject);
            }
        });

return users;
    }

    /**
     * 获取当前登录用户信息
     * @return User 当前用户对象
     */
    protected User getCurrentUser() {
        return userContextUtil.getCurrentUser();
    }

}
