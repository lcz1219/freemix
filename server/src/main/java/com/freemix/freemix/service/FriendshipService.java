package com.freemix.freemix.service;

import com.freemix.freemix.enetiy.Friendship;
import com.freemix.freemix.enetiy.User;
import com.freemix.freemix.util.ApiResponse;
import com.freemix.freemix.util.UserContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 好友关系服务
 * 处理好友申请、通过、拒绝、删除等核心逻辑
 */
@Service
public class FriendshipService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private UserContextUtil userContextUtil;

    /**
     * 获取当前用户名
     */
    private String getCurrentUsername() {
        User user = userContextUtil.getCurrentUser();
        return user != null ? user.getUsername() : null;
    }

    /**
     * 发送好友申请
     * @param toUser 目标用户名
     */
    public ApiResponse sendRequest(String toUser) {
        String fromUser = getCurrentUsername();
        if (fromUser == null) {
            return ApiResponse.failure("用户未登录");
        }
        if (fromUser.equals(toUser)) {
            return ApiResponse.failure("不能添加自己为好友");
        }

        // 检查目标用户是否存在
        User targetUser = mongoTemplate.findOne(
                Query.query(Criteria.where("username").is(toUser)), User.class);
        if (targetUser == null) {
            return ApiResponse.failure("用户不存在");
        }

        // 检查是否已经是好友
        Friendship existing = findFriendship(fromUser, toUser);
        if (existing != null) {
            if ("accepted".equals(existing.getStatus())) {
                return ApiResponse.failure("已经是好友了");
            }
            if ("pending".equals(existing.getStatus())) {
                // 如果对方已经向我发过申请，自动成为好友
                if (fromUser.equals(existing.getToUser())) {
                    existing.setStatus("accepted");
                    existing.setAcceptTime(new Date());
                    mongoTemplate.save(existing);
                    return ApiResponse.success(null, "好友添加成功");
                }
                return ApiResponse.failure("已发送过好友申请，请等待对方确认");
            }
        }

        // 创建新的好友申请
        Friendship friendship = new Friendship();
        friendship.setFromUser(fromUser);
        friendship.setToUser(toUser);
        friendship.setStatus("pending");
        friendship.setRequestTime(new Date());
        friendship.setDel(0);
        mongoTemplate.insert(friendship);

        return ApiResponse.success(null, "好友申请已发送");
    }

    /**
     * 通过好友申请
     * @param requestId 申请ID
     */
    public ApiResponse acceptRequest(String requestId) {
        String currentUser = getCurrentUsername();
        if (currentUser == null) {
            return ApiResponse.failure("用户未登录");
        }

        Friendship friendship = mongoTemplate.findById(requestId, Friendship.class);
        if (friendship == null) {
            return ApiResponse.failure("好友申请不存在");
        }
        if (!currentUser.equals(friendship.getToUser())) {
            return ApiResponse.failure("无权操作该申请");
        }
        if (!"pending".equals(friendship.getStatus())) {
            return ApiResponse.failure("该申请已被处理");
        }

        friendship.setStatus("accepted");
        friendship.setAcceptTime(new Date());
        mongoTemplate.save(friendship);
        return ApiResponse.success(null, "已通过好友申请");
    }

    /**
     * 拒绝好友申请
     * @param requestId 申请ID
     */
    public ApiResponse rejectRequest(String requestId) {
        String currentUser = getCurrentUsername();
        if (currentUser == null) {
            return ApiResponse.failure("用户未登录");
        }

        Friendship friendship = mongoTemplate.findById(requestId, Friendship.class);
        if (friendship == null) {
            return ApiResponse.failure("好友申请不存在");
        }
        if (!currentUser.equals(friendship.getToUser())) {
            return ApiResponse.failure("无权操作该申请");
        }
        if (!"pending".equals(friendship.getStatus())) {
            return ApiResponse.failure("该申请已被处理");
        }

        friendship.setStatus("rejected");
        mongoTemplate.save(friendship);
        return ApiResponse.success(null, "已拒绝好友申请");
    }

    /**
     * 获取好友列表（仅已确认的好友）
     */
    public ApiResponse getFriendList() {
        String currentUser = getCurrentUsername();
        if (currentUser == null) {
            return ApiResponse.failure("用户未登录");
        }

        // 查询双向的好友关系
        Criteria criteria = new Criteria().orOperator(
                Criteria.where("fromUser").is(currentUser),
                Criteria.where("toUser").is(currentUser)
        );
        criteria.and("status").is("accepted");
        criteria.and("del").ne(1);

        List<Friendship> friendships = mongoTemplate.find(
                Query.query(criteria).with(Sort.by(Sort.Direction.DESC, "acceptTime")),
                Friendship.class);

        // 构建好友用户信息列表
        List<Map<String, Object>> friendList = new ArrayList<>();
        for (Friendship fs : friendships) {
            String friendName = currentUser.equals(fs.getFromUser())
                    ? fs.getToUser() : fs.getFromUser();
            User friend = mongoTemplate.findOne(
                    Query.query(Criteria.where("username").is(friendName)), User.class);
            if (friend != null) {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("username", friend.getUsername());
                map.put("chinesename", friend.getChinesename());
                map.put("avatarUrl", friend.getAvatarUrl());
                map.put("fashionTitle", friend.getFashionTitle());
                map.put("becomeFriendTime", fs.getAcceptTime());
                friendList.add(map);
            }
        }
        return ApiResponse.success(friendList);
    }

    /**
     * 获取待处理的好友申请（我收到的）
     */
    public ApiResponse getPendingRequests() {
        String currentUser = getCurrentUsername();
        if (currentUser == null) {
            return ApiResponse.failure("用户未登录");
        }

        List<Friendship> friendships = mongoTemplate.find(
                Query.query(Criteria.where("toUser").is(currentUser)
                        .and("status").is("pending")
                        .and("del").ne(1))
                        .with(Sort.by(Sort.Direction.DESC, "requestTime")),
                Friendship.class);

        // 构建申请人信息列表
        List<Map<String, Object>> result = new ArrayList<>();
        for (Friendship fs : friendships) {
            User fromUser = mongoTemplate.findOne(
                    Query.query(Criteria.where("username").is(fs.getFromUser())), User.class);
            if (fromUser != null) {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("requestId", fs.get_id());
                map.put("username", fromUser.getUsername());
                map.put("chinesename", fromUser.getChinesename());
                map.put("avatarUrl", fromUser.getAvatarUrl());
                map.put("requestTime", fs.getRequestTime());
                result.add(map);
            }
        }
        return ApiResponse.success(result);
    }

    /**
     * 获取待处理申请数量
     */
    public ApiResponse getPendingCount() {
        String currentUser = getCurrentUsername();
        if (currentUser == null) {
            return ApiResponse.failure("用户未登录");
        }

        long count = mongoTemplate.count(
                Query.query(Criteria.where("toUser").is(currentUser)
                        .and("status").is("pending")
                        .and("del").ne(1)),
                Friendship.class);
        return ApiResponse.success(count);
    }

    /**
     * 删除好友（双向软删除）
     * @param friendUsername 好友用户名
     */
    public ApiResponse deleteFriend(String friendUsername) {
        String currentUser = getCurrentUsername();
        if (currentUser == null) {
            return ApiResponse.failure("用户未登录");
        }

        Friendship friendship = findFriendship(currentUser, friendUsername);
        if (friendship == null || !"accepted".equals(friendship.getStatus())) {
            return ApiResponse.failure("好友关系不存在");
        }

        friendship.setDel(1);
        mongoTemplate.save(friendship);
        return ApiResponse.success(null, "好友已删除");
    }

    /**
     * 搜索用户（用于添加好友，只返回非好友且非自己的用户）
     * @param keyword 搜索关键词
     */
    public ApiResponse searchUser(String keyword) {
        String currentUser = getCurrentUsername();
        if (currentUser == null) {
            return ApiResponse.failure("用户未登录");
        }

        if (keyword == null || keyword.trim().isEmpty()) {
            return ApiResponse.success(new ArrayList<>());
        }

        // 搜索匹配的用户
        Criteria criteria = new Criteria().orOperator(
                Criteria.where("username").regex(keyword, "i"),
                Criteria.where("chinesename").regex(keyword, "i")
        );
        List<User> users = mongoTemplate.find(Query.query(criteria), User.class);

        // 过滤掉自己和已经是好友的用户
        List<Map<String, Object>> result = new ArrayList<>();
        for (User user : users) {
            if (user.getUsername().equals(currentUser)) continue;
            
            Friendship fs = findFriendship(currentUser, user.getUsername());
            // 只返回没有好友关系的用户
            if (fs == null || !"accepted".equals(fs.getStatus())) {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("username", user.getUsername());
                map.put("chinesename", user.getChinesename());
                map.put("avatarUrl", user.getAvatarUrl());
                map.put("fashionTitle", user.getFashionTitle());
                // 标记是否有待处理申请
                if (fs != null && "pending".equals(fs.getStatus())) {
                    map.put("requestStatus", currentUser.equals(fs.getFromUser()) ? "sent" : "received");
                }
                result.add(map);
            }
        }
        return ApiResponse.success(result);
    }

    /**
     * 查找两个用户之间的好友关系
     */
    private Friendship findFriendship(String user1, String user2) {
        Criteria criteria = new Criteria().orOperator(
                Criteria.where("fromUser").is(user1).and("toUser").is(user2),
                Criteria.where("fromUser").is(user2).and("toUser").is(user1)
        );
        criteria.and("del").ne(1);
        return mongoTemplate.findOne(Query.query(criteria), Friendship.class);
    }
}
