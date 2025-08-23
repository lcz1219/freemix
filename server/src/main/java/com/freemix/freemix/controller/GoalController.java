package com.freemix.freemix.controller;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.freemix.freemix.CheckToken;
import com.freemix.freemix.enetiy.Goal;
import com.freemix.freemix.enetiy.childGoals;
import com.freemix.freemix.util.ApiResponse;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.bson.json.JsonObject;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Slf4j
@RestController
public class GoalController extends BaseController {
@PostMapping("/editGoal")
@CheckToken
    public ApiResponse editGoal(@RequestBody String body){
        Goal goal = JSONObject.parseObject(body, Goal.class);
        Goal one = mongoTemplate.findOne(new Query(Criteria.where("title").is(goal.getTitle())
                .and("description").is(goal.getDescription()).and("ower").is(goal.getOwner())), Goal.class);
        if(one != null){
            return ApiResponse.failure("已存在相同的目标");
        }
    List<childGoals> childGoals = goal.getChildGoals();
        if(!childGoals.isEmpty()){
            childGoals.forEach(child -> {
                if(StringUtil.isNullOrEmpty(child.get_id())){
                    child.set_id(UUID.randomUUID().toString());
                }
            });
        }
        goal.setChildGoals(childGoals);
    if(StringUtil.isNullOrEmpty(goal.get_id())){
            goal.set_id(UUID.randomUUID().toString());
            goal.setProgress(0);
            goal.setStatus("in-progress");


            mongoTemplate.insert(goal);
        }else {

            if (goal.getProgress()==100){
                goal.setStatus("completed");
            }


            mongoTemplate.save(goal);
        }

        return ApiResponse.success(body);
    }

@GetMapping("/getGoals/{ower}")
@CheckToken
    public ApiResponse getGoals(@PathVariable("ower") String ower){
    List<Goal> owner = mongoTemplate.find(new Query(Criteria.where("owner").is(ower)), Goal.class);
    owner.stream().forEach(goal -> {
        if(goal.getProgress()==100){
            goal.setFinish(true);
        }else {
            goal.setFinish(false);
        }
       if(System.currentTimeMillis()>goal.getDeadline().getTime()){
           goal.setStatus("expired");


//           editGoal(goal.toString());
       }
    });
    return ApiResponse.success(owner);

}
    @PostMapping("/saveGoalStructure")
    public ApiResponse saveGoalStructure(@RequestBody String body) {
        log.info("saveGoalStructure---{}", body);
        JSONObject jsonObject = JSONObject.parseObject(body);

        // 1. 解析前端数据
        JSONArray nodes = jsonObject.getJSONArray("nodes");
        JSONArray connections = jsonObject.getJSONArray("connections");

        // 2. 动态识别根节点
        String rootNodeId = identifyRootNode(nodes, connections);
        if (rootNodeId == null) {
            throw new IllegalArgumentException("无法识别根节点");
        }

        // 3. 查找目标对象（基于根节点ID）
        Goal goal = mongoTemplate.findOne(Query.query(Criteria.where("_id").is(rootNodeId)), Goal.class);

        // 4. 构建节点映射
        Map<String, JSONObject> nodeMap = new HashMap<>();
        for (int i = 0; i < nodes.size(); i++) {
            JSONObject node = nodes.getJSONObject(i);
            nodeMap.put(node.getString("id"), node);
        }

        // 5. 构建连接关系映射
        Map<String, List<String>> connectionMap = new HashMap<>();
        Map<String, String> parentMap = new HashMap<>(); // 记录每个节点的父节点

        for (int i = 0; i < connections.size(); i++) {
            JSONObject conn = connections.getJSONObject(i);
            String source = conn.getString("source");
            String target = conn.getString("target");

            connectionMap.computeIfAbsent(source, k -> new ArrayList<>()).add(target);
            parentMap.put(target, source); // 记录父节点
        }

        // 6. 更新目标基本信息
        JSONObject rootNode = nodeMap.get(rootNodeId);
        if (rootNode != null) {
            goal.setTitle(rootNode.getString("title"));
            goal.setDescription(rootNode.getString("description"));
        }

        // 7. 构建新的子目标结构
        List<childGoals> newChildGoals = new ArrayList<>();

        // 8. 处理直接连接到根节点的子目标（一级子目标）
        if (connectionMap.containsKey(rootNodeId)) {
            for (String childId : connectionMap.get(rootNodeId)) {
                JSONObject childNode = nodeMap.get(childId);
                if (childNode != null) {
                    childGoals childGoal = createOrUpdateChildGoal(childNode, goal);
                    newChildGoals.add(childGoal);

                    // 9. 处理二级子目标
                    processNestedChildren(childGoal, childId, connectionMap, nodeMap);
                }
            }
        }

        // 10. 更新目标结构
        goal.setChildGoals(newChildGoals);
        Integer newProgress = computedProgress(goal);
        goal.setProgress(newProgress);
        if(goal.getProgress()==100){
            goal.setStatus("completed");
        }else{
            goal.setStatus("in-progress");
        }


        // 11. 保存更新
        mongoTemplate.save(goal);
        return ApiResponse.success("目标结构保存成功");
    }
    private Integer computedProgress(Goal goal) {
       Integer total=goal.getChildGoals().size();
        int size = goal.getChildGoals().stream().filter(e -> e.isFinish()).collect(Collectors.toList()).size();
        int i = (size*100 / total*100);
        return i/100;

    }
    // 处理嵌套子目标
    private void processNestedChildren(childGoals parentGoal, String parentId,
                                       Map<String, List<String>> connectionMap,
                                       Map<String, JSONObject> nodeMap) {
        if (connectionMap.containsKey(parentId)) {
            List<childGoals> nestedChildren = new ArrayList<>();


            for (String childId : connectionMap.get(parentId)) {
                JSONObject childNode = nodeMap.get(childId);
                if (childNode != null) {
                    childGoals childGoal = new childGoals();
                    childGoal.set_id(childId);
                    childGoal.setMessage(childNode.getString("title"));
                    childGoal.setFinish(false);

                    nestedChildren.add(childGoal);

                    // 递归处理更深层次的子目标
                    processNestedChildren(childGoal, childId, connectionMap, nodeMap);
                }
            }

            parentGoal.setChildGoals(nestedChildren);
        }
    }
    // 递归查找子目标
    private childGoals findExistingChild(String id, List<childGoals> parentGoal) {
        for (childGoals child : parentGoal) {
            if (child.get_id().equals(id)) {
                return child;
            }

            // 递归查找嵌套子目标
            if (child.getChildGoals() != null) {
                childGoals nested = findExistingChild(id, child.getChildGoals());
                if (nested != null) return nested;
            }
        }
        return null;
    }
    // 在创建或更新子目标时保留完成状态
    private childGoals createOrUpdateChildGoal(JSONObject node, Goal parentGoal) {
        childGoals existing = findExistingChild(node.getString("id"), parentGoal.getChildGoals());

        if (existing != null) {
            existing.setMessage(node.getString("title"));
            return existing;
        } else {
            childGoals newChild = new childGoals();
            newChild.set_id(node.getString("id"));
            newChild.setMessage(node.getString("title"));
            newChild.setFinish(false); // 新节点默认未完成
            return newChild;
        }
    }
    private String identifyRootNode(JSONArray nodes, JSONArray connections) {
        // 1. 收集所有节点ID
        Set<String> allNodeIds = new HashSet<>();
        for (int i = 0; i < nodes.size(); i++) {
            JSONObject node = nodes.getJSONObject(i);
            allNodeIds.add(node.getString("id"));
        }

        // 2. 收集所有有父节点的节点
        Set<String> childNodes = new HashSet<>();
        for (int i = 0; i < connections.size(); i++) {
            JSONObject conn = connections.getJSONObject(i);
            childNodes.add(conn.getString("target"));
        }

        // 3. 找出没有父节点的节点（根节点候选）
        Set<String> rootCandidates = new HashSet<>(allNodeIds);
        rootCandidates.removeAll(childNodes);

        // 4. 处理根节点候选
        if (rootCandidates.isEmpty()) {
            // 没有明显的根节点，可能是循环结构
            return findMostCentralNode(allNodeIds, connections);
        } else if (rootCandidates.size() == 1) {
            // 只有一个候选，直接返回
            return rootCandidates.iterator().next();
        } else {
            // 多个候选，选择连接最多的节点
            return findMostConnectedNode(rootCandidates, connections);
        }
    }
    private String findMostConnectedNode(Set<String> candidates, JSONArray connections) {
        Map<String, Integer> connectionCount = new HashMap<>();

        // 初始化计数
        for (String candidate : candidates) {
            connectionCount.put(candidate, 0);
        }

        // 统计连接数
        for (int i = 0; i < connections.size(); i++) {
            JSONObject conn = connections.getJSONObject(i);
            String source = conn.getString("source");

            if (candidates.contains(source)) {
                connectionCount.put(source, connectionCount.get(source) + 1);
            }
        }

        // 返回连接数最多的节点
        return Collections.max(connectionCount.entrySet(), Map.Entry.comparingByValue()).getKey();
    }

    // 查找最中心的节点（当没有明显根节点时）
    private String findMostCentralNode(Set<String> allNodeIds, JSONArray connections) {
        // 构建节点连接关系
        Map<String, Set<String>> neighbors = new HashMap<>();
        for (String nodeId : allNodeIds) {
            neighbors.put(nodeId, new HashSet<>());
        }

        // 添加连接关系
        for (int i = 0; i < connections.size(); i++) {
            JSONObject conn = connections.getJSONObject(i);
            String source = conn.getString("source");
            String target = conn.getString("target");

            neighbors.get(source).add(target);
            neighbors.get(target).add(source); // 双向连接
        }

        // 计算每个节点的中心度（连接数）
        String mostCentral = null;
        int maxConnections = -1;

        for (Map.Entry<String, Set<String>> entry : neighbors.entrySet()) {
            if (entry.getValue().size() > maxConnections) {
                maxConnections = entry.getValue().size();
                mostCentral = entry.getKey();
            }
        }

        return mostCentral;
    }

}
