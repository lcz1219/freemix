package com.freemix.freemix.controller;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.freemix.freemix.CheckToken;
import com.freemix.freemix.enetiy.*;
import com.freemix.freemix.service.AchievementService;
import com.freemix.freemix.util.ApiResponse;
import io.netty.util.internal.StringUtil;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.bson.json.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Slf4j
@RestController
public class GoalController extends BaseController {
    @PostMapping("/editGoal")
    @CheckToken
    public ApiResponse editGoal(@RequestBody String body) {
        Goal goal = JSONObject.parseObject(body, Goal.class);
        if(GoalStauts.expired.equals(goal.getStatus())||System.currentTimeMillis()>goal.getDeadline().getTime()){
            return ApiResponse.failure("该目标已经过期，无法操作");
        }
        List<Achievement> unlockedAchievements = new ArrayList<>();
        ApiResponse<Object> res = getObjectApiResponse(goal, unlockedAchievements);
        if (res != null) return res;

        ApiResponse response = ApiResponse.success(body,"修改成功");
        if (!unlockedAchievements.isEmpty()) {
            response.setAchievements(unlockedAchievements);
        }
        return response;
    }

    private ApiResponse<Object> getObjectApiResponse(Goal goal, List<Achievement> unlockedAchievements) {
        Goal one = mongoTemplate.findOne(new Query(Criteria.where("title").is(goal.getTitle())
                .and("description").is(goal.getDescription()).and("ower").is(goal.getOwner())), Goal.class);
        if (one != null) {
            return ApiResponse.failure("已存在相同的目标");
        }
        List<childGoals> childGoals = goal.getChildGoals();
        if (!childGoals.isEmpty()) {
            childGoals.forEach(child -> {
                if (StringUtil.isNullOrEmpty(child.get_id())) {
                    child.set_id(UUID.randomUUID().toString());
                }
            });
        }
        goal.setChildGoals(childGoals);
        if (StringUtil.isNullOrEmpty(goal.get_id())) {
            goal.set_id(UUID.randomUUID().toString());
            goal.setProgress(0);
            goal.setStatus("in-progress");

            
            // 触发创建目标成就
            try {
                if (unlockedAchievements != null) {
                    unlockedAchievements.addAll(achievementService.checkAndUnlock(goal.getOwner(), "GOAL_CREATE", goal));
                } else {
                    achievementService.checkAndUnlock(goal.getOwner(), "GOAL_CREATE", goal);
                }
            } catch (Exception e) {
                log.error("触发创建目标成就失败", e);
            }

            mongoTemplate.insert(goal);
        } else {
            if (System.currentTimeMillis() > goal.getDeadline().getTime()) {
                goal.setStatus("expired");
            } else {
                goal.setStatus("in-progress");
            }

            if (goal.getProgress() == 100) {
                goal.setStatus("completed");
            }
            Integer newProgress = computedProgress(goal);
            goal.setProgress(newProgress);

            
            // 触发完成目标成就 (如果是更新导致完成)
            if ("completed".equals(goal.getStatus())) {
                try {
                    if (unlockedAchievements != null) {
                        unlockedAchievements.addAll(achievementService.checkAndUnlock(goal.getOwner(), "GOAL_FINISH", goal));
                    } else {
                        achievementService.checkAndUnlock(goal.getOwner(), "GOAL_FINISH", goal);
                    }
                } catch (Exception e) {
                    log.error("触发完成目标成就失败", e);
                }
            }

            mongoTemplate.save(goal);
        }
        //插入创建者到关系表里面
        editCollaborator(goal.get_id(), Arrays.asList(goal.getOwner()), "owner");
        return null;
    }


    /**
     * 从Excel文件导入目标
     *
     * @param file  Excel文件
     * @param owner 目标所有者
     * @return 导入结果
     */
    @PostMapping("/importGoalsFromExcel")
    @CheckToken
    public ApiResponse importGoalsFromExcel(@RequestParam("file") MultipartFile file,
                                            @RequestParam("owner") String owner) {
        try {
            // 检查文件是否为空
            if (file.isEmpty()) {
                return ApiResponse.failure("上传的文件为空");
            }

            // 检查文件格式
            String fileName = file.getOriginalFilename();
            if (fileName == null || (!fileName.toLowerCase().endsWith(".xlsx") && !fileName.toLowerCase().endsWith(".xls"))) {
                return ApiResponse.failure("请上传.xlsx或.xls格式的Excel文件");
            }

            JSONObject res = parseGoalsFromExcel(file, owner);
            Boolean ischeck = res.getBoolean("ischeck");
            String ischeckMsg = res.getString("ischeckMsg");
            if (!ischeck) return ApiResponse.failure("导入失败: " + ischeckMsg);
            List<Goal> goals = res.getJSONArray("goals").toJavaList(Goal.class);
            List<Goal> savedGoals = new ArrayList<>();

            for (Goal goal : goals) {
                // 检查是否已存在相同目标
//                Goal existingGoal = mongoTemplate.findOne(
//                    new Query(Criteria.where("title").is(goal.getTitle())
//                            .and("description").is(goal.getDescription())
//                            .and("owner").is(goal.getOwner())),
//                    Goal.class);

//                if (existingGoal == null) {
                // 设置目标ID和初始状态
                goal.set_id(UUID.randomUUID().toString());
                goal.setProgress(0);
                goal.setStatus("in-progress");

                // 处理子目标ID
                if (goal.getChildGoals() != null && !goal.getChildGoals().isEmpty()) {
                    goal.getChildGoals().forEach(child -> {
                        if (StringUtil.isNullOrEmpty(child.get_id())) {
                            child.set_id(UUID.randomUUID().toString());
                        }
                    });
                }

                // 插入创建者到关系表
                editCollaborator(goal.get_id(), Arrays.asList(goal.getOwner()), "owner");

                mongoTemplate.insert(goal);
                savedGoals.add(goal);
            }
//            }

            return ApiResponse.success("成功导入 " + savedGoals.size() + " 个目标");
        } catch (Exception e) {
            log.error("导入Excel目标时发生错误", e);
            return ApiResponse.failure("导入失败: " + e.getMessage());
        }
    }


    /**
     * 解析Excel文件中的目标数据
     *
     * @param file  Excel文件
     * @param owner 目标所有者
     * @return 目标列表
     * @throws Exception 解析异常
     */
    private JSONObject parseGoalsFromExcel(MultipartFile file, String owner) throws Exception {
        List<Goal> goals = new ArrayList<>();
        List<childGoals> orphanedChildGoals = new ArrayList<>(); // 存储尚未找到父目标的子目标
        Workbook workbook;
        boolean ischeck = true;
        String ischeckMsg = "";

        // 根据文件扩展名选择合适的工作簿类型
        String fileName = file.getOriginalFilename();
        if (fileName != null && fileName.toLowerCase().endsWith(".xls")) {
            // 处理.xls文件（Excel 97-2003格式）
            workbook = new HSSFWorkbook(file.getInputStream());
        } else {
            // 处理.xlsx文件（Excel 2007+格式）
            workbook = new XSSFWorkbook(file.getInputStream());
        }

        Sheet sheet = workbook.getSheetAt(0); // 读取第一个工作表

        // 跳过标题行，从第二行开始读取数据
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;
            // 新增：获取最后一列的数据
            // 1. 首先获取最后一列的索引（从0开始）
            int lastCellIndex = row.getLastCellNum() - 1;
            String cellValueAsString = getCellValueAsString(row.getCell(lastCellIndex));
            boolean isChildGoal = true;
           if(cellValueAsString==null || cellValueAsString.isEmpty()) isChildGoal = false;

            // 检查是否为子目标（第一列为空）

            String firstColumnValue = getCellValueAsString(row.getCell(0));

            if (isChildGoal) {
                // 这是子目标
                childGoals childGoal = new childGoals();
                childGoal.setMessage(getCellValueAsString(row.getCell(1)));
                childGoal.setExcelParentTitle(getCellValueAsString(row.getCell(lastCellIndex)));
                // 解析子目标截止日期
                Cell deadlineCell = row.getCell(2);
                if (deadlineCell != null) {
                    if (deadlineCell.getCellType() == CellType.NUMERIC) {
                        // 注意：childGoals类中没有deadline字段，需要根据实际情况处理
                        // 这里仅作演示，实际可能需要添加deadline字段或使用其他方式存储
                    } else {
                        String dateStr = getCellValueAsString(deadlineCell);
                        if (dateStr != null && !dateStr.isEmpty()) {
                            // 同样，childGoals类中没有deadline字段，需要根据实际情况处理
                        }
                    }
                }

                // 解析子目标优先级
//                childGoal.setPriority(getCellValueAsString(row.getCell(3)));

                // 解析子目标标签
                String tagsStr = getCellValueAsString(row.getCell(4));
                if (tagsStr != null && !tagsStr.isEmpty()) {
                    String[] tagsArray = tagsStr.split(",");
                    List<String> tags = new ArrayList<>();
                    for (String tag : tagsArray) {
                        tags.add(tag.trim());
                    }
//                    childGoal.setTags(tags);
                } else {
                    if (ischeckMsg.isEmpty()) {
                        ischeckMsg = ischeckMsg + "子目标标签未填写,请填写后再次导入";
                    } else {
                        ischeckMsg = ischeckMsg + ",子目标标签未填写,请填写后再次导入";
                    }
                    ischeck = false;
                }

                // 初始化子目标的其他字段
                childGoal.setChildGoals(new ArrayList<>());
                childGoal.setFileList(new ArrayList<>());
                childGoal.setFinish(false);

                // 尝试将子目标关联到已存在的父目标
                boolean attached = false;
                for (Goal goal : goals) {
                    if (goal.getTitle().equals(childGoal.getExcelParentTitle())) {
                        goal.getChildGoals().add(childGoal);
                        attached = true;
                        break;
                    }
                }

                // 如果未找到父目标，将子目标添加到孤儿列表中
                if (!attached) {
                    orphanedChildGoals.add(childGoal);
                }
            } else {
                // 这是主目标
                Goal goal = new Goal();
                goal.setTitle(firstColumnValue);
                goal.setDescription(getCellValueAsString(row.getCell(1)));
                goal.setOwner(owner);

                // 解析截止日期
                Cell deadlineCell = row.getCell(2);
                if (deadlineCell != null) {
                    if (deadlineCell.getCellType() == CellType.NUMERIC) {
                        goal.setDeadline(deadlineCell.getDateCellValue());
                    } else {
                        String dateStr = getCellValueAsString(deadlineCell);
                        if (dateStr != null && !dateStr.isEmpty()) {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                            goal.setDeadline(sdf.parse(dateStr));
                        } else {
                            ischeckMsg = ischeckMsg + "截止日期未填写,请填写后再次导入";
                            ischeck = false;
                        }
                    }
                }

                // 解析优先级
                goal.setLevel(getCellValueAsString(row.getCell(3)));

                // 解析标签
                String tagsStr = getCellValueAsString(row.getCell(4));
                if (tagsStr != null && !tagsStr.isEmpty()) {
                    String[] tagsArray = tagsStr.split(",");
                    List<String> tags = new ArrayList<>();
                    for (String tag : tagsArray) {
                        tags.add(tag.trim());
                    }
                    goal.setTags(tags);
                } else {
                    if (ischeckMsg.isEmpty()) {
                        ischeckMsg = ischeckMsg + "标签未填写,请填写后再次导入";
                    } else {
                        ischeckMsg = ischeckMsg + ",标签未填写,请填写后再次导入";
                    }

                    ischeck = false;
                }

                // 初始化其他字段
                goal.setChildGoals(new ArrayList<>());
                goal.setFileList(new ArrayList<>());
                goal.setCollaborators(new ArrayList<>());
                goal.setPlanTime(0);
                goal.setDel(0);
                goal.setDisRecover(false);
                goal.setFinish(false);

                goals.add(goal);

                // 检查是否有孤儿子目标可以关联到这个新创建的父目标
                Iterator<childGoals> iterator = orphanedChildGoals.iterator();
                while (iterator.hasNext()) {
                    childGoals orphanedChild = iterator.next();
                    if (orphanedChild.getExcelParentTitle().equals(goal.getTitle())) {
                        goal.getChildGoals().add(orphanedChild);
                        iterator.remove(); // 从孤儿列表中移除
                    }
                }
            }
        }

        // 检查是否还有未关联的孤儿子目标
        if (!orphanedChildGoals.isEmpty()) {
            if (ischeckMsg.isEmpty()) {
                ischeckMsg = ischeckMsg + "存在未找到父目标的子目标，请检查Excel文件格式";
            } else {
                ischeckMsg = ischeckMsg + ",存在未找到父目标的子目标，请检查Excel文件格式";
            }
            ischeck = false;
        }

        workbook.close();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("goals", goals);
        jsonObject.put("ischeck", ischeck);
        jsonObject.put("ischeckMsg", ischeckMsg);
        return jsonObject;
    }

    /**
     * 获取单元格值作为字符串
     *
     * @param cell 单元格
     * @return 单元格值字符串
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) return null;

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    return sdf.format(cell.getDateCellValue());
                } else {
                    return String.valueOf((long) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return null;
        }
    }

    @PostMapping("/deleteGoal")
    @CheckToken
    public ApiResponse deleteGoal(@RequestBody String body) {
        JSONObject bodyJson = JSONObject.parseObject(body);
        Goal goal = JSONObject.parseObject(bodyJson.getString("row"), Goal.class);
        log.info("deleteGoal {}", goal);
        goal.setDel(1);
        goal.setDeltime(System.currentTimeMillis());
        mongoTemplate.save(goal);
        return ApiResponse.success(body);
    }

    @GetMapping("/recycle/{ower}")
    @CheckToken
    public ApiResponse recycle(@PathVariable("ower") String ower) {

        List<Goal> owner = mongoTemplate.find(new Query(Criteria.where("owner").is(ower).and("del").is(1)), Goal.class);
        owner.stream().forEach(goal -> {
            if (goal.getProgress() == 100) {
                goal.setFinish(true);
            } else {
                goal.setFinish(false);
            }
            if (System.currentTimeMillis() > goal.getDeadline().getTime()) {
                goal.setStatus("expired");


//           editGoal(goal.toString());
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String format = sdf.format(goal.getDeltime());
            goal.setDelDate(format);
            if (goal.getDeltime() != 0L && System.currentTimeMillis() - goal.getDeltime() > 30 * 24 * 60 * 60 * 1000L) {
                goal.setDisRecover(true);
            }
        });
        return ApiResponse.success(owner);
    }

    @GetMapping("/getGoals/{ower}")
    @CheckToken
    public ApiResponse getGoals(@PathVariable("ower") String ower) {
        List<String> collect = mongoTemplate.find(new Query(Criteria.where("username").is(ower).and("del").ne(1)), Relation.class)
                .stream()
                .map(Relation::getGoalId)
                .collect(Collectors.toList());
        List<Goal> owner = mongoTemplate.find(new Query(new Criteria().orOperator(
                Criteria.where("owner").is(ower).and("del").ne(1),
                Criteria.where("_id").in(collect).and("del").ne(1)
        )), Goal.class);
        owner.stream().forEach(goal -> {
            if (goal.getProgress() == 100) {
                goal.setFinish(true);
            } else {
                goal.setFinish(false);
            }
            goal.setCollaborators(genCollaborator(goal.get_id()));
            sortChildGoals(goal);
           switch (goal.getStatus()){
               case "expired":
                   goal.setStatuslevel(3);
                   break;
               case "completed":
                   goal.setStatuslevel(2);
                   break;
               case "in-progress":
                   goal.setStatuslevel(1);
                   break;
           }

        });
        owner = owner.stream().sorted(Comparator.comparingInt(Goal::getStatuslevel)).collect(Collectors.toList());
        return ApiResponse.success(owner);

    }

@Autowired
AchievementService achievementService;

    @PostMapping("/addCollaborator")
    @CheckToken
    public ApiResponse addCollaborator(@RequestBody String body) {
        JSONObject jsonObject = JSONObject.parseObject(body);
        String username = jsonObject.getString("username");
        String goalId = jsonObject.getString("goalId");
        editCollaborator(goalId, Arrays.asList(username), "collaborator");
        User username1 = mongoTemplate.findOne(new Query(Criteria.where("username").is(username)), User.class);

        // 触发社交成就
        List<Achievement> unlockedAchievements = new ArrayList<>();
        try {
            Goal goal = mongoTemplate.findOne(new Query(Criteria.where("_id").is(goalId)), Goal.class);
            if (goal != null) {
                unlockedAchievements.addAll(achievementService.checkAndUnlock(goal.getOwner(), "ADD_COLLABORATOR", null));
            }
        } catch (Exception e) {
            log.error("触发社交成就失败", e);
        }

        ApiResponse response = ApiResponse.success(username1);
        if (!unlockedAchievements.isEmpty()) {
            response.setAchievements(unlockedAchievements);
        }
        return response;

    }

    @PostMapping("/removeCollaborator")
    @CheckToken
    public ApiResponse removeCollaborator(@RequestBody String body) {
        JSONObject jsonObject = JSONObject.parseObject(body);
        String username = jsonObject.getString("username");
        String goalId = jsonObject.getString("goalId");
        delRelationPerson(username, goalId);

        return ApiResponse.success(goalId);

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
        if (goal.getProgress() == 100) {
            goal.setStatus("completed");
        } else {
            goal.setStatus("in-progress");
        }


        // 11. 保存更新
        mongoTemplate.save(goal);
        return ApiResponse.success("目标结构保存成功");
    }

    private Integer computedProgress(Goal goal) {
        Integer total = goal.getChildGoals().size();
        int size = goal.getChildGoals().stream().filter(e -> e.isFinish()).collect(Collectors.toList()).size();
        int i = (size * 100 / total * 100);
        return i / 100;

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


    @PostMapping("/finishGoal")
    @CheckToken
    public ApiResponse finishGoal(@RequestBody String body){
        JSONObject jsonObject = JSONObject.parseObject(body);
        String goalId = jsonObject.getString("goalId");
        String type = jsonObject.getString("type");

        Goal id = mongoTemplate.findOne(new Query().addCriteria(Criteria.where("_id").is(goalId)), Goal.class);

        if("success".equals(type)){
            id.getChildGoals().stream().forEach(childGoalId -> {
                childGoalId.setFinish(true);
            });

        }else{
            List<String> childGoalIds= jsonObject.getJSONArray("childGoalIds").toList(String.class);

            List<childGoals> childGoals = id.getChildGoals();
            childGoals.stream().forEach(child -> {
                if(childGoalIds.contains(child.get_id())){
                    child.setFinish(true);
                }else{
                    child.setFinish(false);
                }
            });
            id.setChildGoals(childGoals);
        }
        List<Achievement> unlockedAchievements = new ArrayList<>();
        getObjectApiResponse(id, unlockedAchievements);
        ApiResponse response = ApiResponse.success();
        if (!unlockedAchievements.isEmpty()) {
            response.setAchievements(unlockedAchievements);
        }
        return response;


    }

}
