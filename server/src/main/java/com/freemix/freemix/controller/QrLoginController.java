package com.freemix.freemix.controller;

import com.alibaba.fastjson2.JSONObject;
import com.freemix.freemix.CheckToken;
import com.freemix.freemix.enetiy.User;
import com.freemix.freemix.util.ApiResponse;
import com.freemix.freemix.util.JwtUtil;
import com.freemix.freemix.util.UserContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
public class QrLoginController {

    private static final String QR_LOGIN_KEY_PREFIX = "qr_login_session:";
    private static final long QR_SESSION_TTL_SECONDS = 120;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private UserContextUtil userContextUtil;

    @PostMapping("/qr-login/create")
    public ApiResponse createQrLoginSession(@RequestBody(required = false) String body) {
        String sessionId = UUID.randomUUID().toString();
        String sessionToken = JwtUtil.generateQrSessionToken(sessionId, QR_SESSION_TTL_SECONDS);

        JSONObject session = new JSONObject();
        session.put("status", "PENDING");
        session.put("createdAt", new Date().getTime());

        String key = QR_LOGIN_KEY_PREFIX + sessionId;
        redisTemplate.opsForValue().set(key, session.toJSONString(), QR_SESSION_TTL_SECONDS, TimeUnit.SECONDS);

        JSONObject result = new JSONObject();
        result.put("sessionId", sessionId);
        result.put("sessionToken", sessionToken);
        result.put("expiresIn", QR_SESSION_TTL_SECONDS);

        return ApiResponse.success(result);
    }

    @PostMapping("/qr-login/confirm")
    @CheckToken
    public ApiResponse confirmQrLogin(@RequestBody String body) {
        JSONObject json = JSONObject.parseObject(body);
        String sessionId = json.getString("sessionId");
        String sessionToken = json.getString("sessionToken");

        if (sessionId == null || sessionToken == null) {
            return ApiResponse.failure("二维码参数无效");
        }

        boolean valid = JwtUtil.validateQrSessionToken(sessionToken, sessionId);
        if (!valid) {
            return ApiResponse.failure("二维码已失效");
        }

        String key = QR_LOGIN_KEY_PREFIX + sessionId;
        String sessionJson = redisTemplate.opsForValue().get(key);
        if (sessionJson == null) {
            return ApiResponse.failure("二维码已过期");
        }

        JSONObject session = JSONObject.parseObject(sessionJson);
        String status = session.getString("status");
        if (!"PENDING".equals(status)) {
            return ApiResponse.failure("二维码已使用");
        }

        User currentUser = userContextUtil.getCurrentUser();
        if (currentUser == null) {
            return ApiResponse.failure("用户未登录");
        }

        // 生成一个临时的web token，用于握手，这个token有效期60分钟
        String tempWebToken = UUID.randomUUID().toString();
        currentUser.setToken(tempWebToken);

        mongoTemplate.findAndModify(
                new Query().addCriteria(Criteria.where("id").is(currentUser.getId())),
                new Update().set("token", tempWebToken),
                User.class
        );

        String tokenKey = currentUser.getUsername() + "_token";
        redisTemplate.opsForValue().set(tokenKey, tempWebToken, 60, TimeUnit.MINUTES);

        JSONObject sanitizedUser = new JSONObject();
        sanitizedUser.put("id", currentUser.getId());
        sanitizedUser.put("username", currentUser.getUsername());
        sanitizedUser.put("avatarUrl", currentUser.getAvatarUrl());
        sanitizedUser.put("email", currentUser.getEmail());
        // 返回给前端的token，前端如果是桌面端会拿这个去换取长期的deskToken
        sanitizedUser.put("token", tempWebToken); 
        sanitizedUser.put("fashionTitle", currentUser.getFashionTitle());

        session.put("status", "APPROVED");
        session.put("username", currentUser.getUsername());
        session.put("userId", currentUser.getId());
        session.put("token", tempWebToken);
        session.put("user", sanitizedUser);

        redisTemplate.opsForValue().set(key, session.toJSONString(), 60, TimeUnit.SECONDS);

        return ApiResponse.success(true);
    }

    @GetMapping("/qr-login/status")
    public ApiResponse getQrLoginStatus(@RequestParam("sessionId") String sessionId) {
        String key = QR_LOGIN_KEY_PREFIX + sessionId;
        String sessionJson = redisTemplate.opsForValue().get(key);
        if (sessionJson == null) {
            JSONObject expired = new JSONObject();
            expired.put("status", "EXPIRED");
            return ApiResponse.success(expired);
        }

        JSONObject session = JSONObject.parseObject(sessionJson);
        String status = session.getString("status");

        JSONObject result = new JSONObject();
        result.put("status", status);

        if ("APPROVED".equals(status)) {
            result.put("username", session.getString("username"));
            result.put("token", session.getString("token"));
            result.put("user", session.getJSONObject("user"));
        }

        return ApiResponse.success(result);
    }
}

