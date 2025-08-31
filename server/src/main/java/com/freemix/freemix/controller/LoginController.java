package com.freemix.freemix.controller;

import com.alibaba.fastjson2.JSONObject;
import com.freemix.freemix.CheckToken;
import com.freemix.freemix.enetiy.LoginLog;
import com.freemix.freemix.enetiy.User;
import com.freemix.freemix.service.LoginLogService;
import com.freemix.freemix.util.ApiResponse;
import com.freemix.freemix.util.CaptchaUtil;
import com.freemix.freemix.util.GoogleAuthenticatorUtil;
import com.freemix.freemix.util.LoginLogUtil;
import io.lettuce.core.json.JsonObject;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@RestController("")
public class LoginController {
    @Autowired
    MongoTemplate mongoTemplate;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    LoginLogService loginLogService;
    @Autowired
    HttpServletRequest request;
    
    @PostMapping("/register")
    public ApiResponse register(@RequestBody  String body){
        User user = JSONObject.parseObject(body, User.class);
        User existUser = mongoTemplate.findOne(new Query().addCriteria(Criteria.where("name").is(user.getUsername())), User.class);
        if(existUser != null){
            return ApiResponse.failure("此用户名已存在");
        }
        mongoTemplate.insert(user);
        return ApiResponse.success(user);

    }
    
    @PostMapping("/login")
    public ApiResponse login(@RequestBody  String body){
        // 初始化登录日志对象
        LoginLog loginLog = new LoginLog();
        
        // 获取请求信息
        String userAgent = request.getHeader("User-Agent");
        String ipAddress = LoginLogUtil.getClientIp(request);
        
        // 设置日志基本信息
        loginLog.setIpAddress(ipAddress);
        loginLog.setUserAgent(userAgent);
        loginLog.setDeviceType(LoginLogUtil.getDeviceType(userAgent));
        loginLog.setBrowser(LoginLogUtil.getBrowser(userAgent));
        loginLog.setOs(LoginLogUtil.getOperatingSystem(userAgent));
        
        JSONObject jsonObject = JSONObject.parseObject(body);
        String username = jsonObject.getString("username");
        String password = jsonObject.getString("password");
        String captcha = jsonObject.getString("captcha");
        String totpCode = jsonObject.getString("totpCode"); // Google Authenticator验证码
        
        // 设置用户名
        loginLog.setUsername(username);
        
        try {
            // 验证验证码
            String captchaKey = "captcha_" + username;
            String storedCaptcha = (String) redisTemplate.opsForValue().get(captchaKey);
            if(StringUtils.isEmpty(totpCode)){
                if (storedCaptcha == null || !storedCaptcha.equalsIgnoreCase(captcha)) {
                    loginLog.setLoginSuccess(false);
                    loginLog.setErrorMessage("验证码错误");
                    loginLogService.saveLoginLog(loginLog);
                    return ApiResponse.failure("验证码错误");
                }
            }
            
            // 验证成功后删除验证码
            redisTemplate.delete(captchaKey);
            
            User user = new User();
            user.setUsername(username);
            user.setPassword(password);
            
            User userFromDB = mongoTemplate.findOne(new Query().addCriteria(Criteria.where("username").is(user.getUsername())
                    .and("password").is(user.getPassword())), User.class);
            if(userFromDB == null){
                loginLog.setLoginSuccess(false);
                loginLog.setErrorMessage("用户名或密码错误");
                loginLogService.saveLoginLog(loginLog);
                return ApiResponse.failure("登录失败");
            }
            
            // 设置用户ID
            loginLog.setUserId(userFromDB.getId());
            
            // 如果用户启用了2FA，则验证TOTP代码
////        if (userFromDB.isTwoFactorEnabled()) {
//            if (totpCode == null || totpCode.isEmpty()) {
//                // 如果需要2FA但未提供TOTP代码，则返回特殊响应
//                JSONObject result = new JSONObject();
//                result.put("secretKey",userFromDB.getSecretKey());
//                result.put("require2FA", true);
//                result.put("userId", userFromDB.getId());
//                return ApiResponse.success(result, "需要输入双因素认证码");
//            }

//            // 验证TOTP代码
//            if (!GoogleAuthenticatorUtil.verifyCode(userFromDB.getSecretKey(), Long.parseLong(totpCode))) {
//                loginLog.setLoginSuccess(false);
//                loginLog.setErrorMessage("双因素认证码错误");
//                loginLogService.saveLoginLog(loginLog);
//                return ApiResponse.failure("双因素认证码错误");
//            }
//        }
            
            userFromDB.setToken(UUID.randomUUID().toString());
            redisTemplate.opsForValue().set("token", userFromDB.getToken(),60, TimeUnit.MINUTES);
            // 更新用户信息
            mongoTemplate.findAndModify(
                new Query().addCriteria(Criteria.where("id").is(userFromDB.getId())),
                new Update().set("token", userFromDB.getToken()),
                User.class
            );
            
            // 登录成功，更新日志状态
            loginLog.setLoginSuccess(true);
            loginLogService.saveLoginLog(loginLog);
            
            return ApiResponse.success(userFromDB);
        } catch (Exception e) {
            // 捕获其他异常
            loginLog.setLoginSuccess(false);
            loginLog.setErrorMessage("登录过程中发生错误: " + e.getMessage());
            loginLogService.saveLoginLog(loginLog);
            log.error("登录异常: {}", e.getMessage(), e);
            return ApiResponse.failure("登录失败，请稍后重试");
        }
    }
    
    @PostMapping("/captcha")
    public ApiResponse getCaptcha(@RequestBody String body) {
        // 生成验证码
        CaptchaUtil.CaptchaResult captchaResult = CaptchaUtil.generateCaptcha();
        // 存储验证码到Redis，设置5分钟过期
        JSONObject jsonObject = JSONObject.parseObject(body);
        String username = jsonObject.getString("username");
        String captchaKey = "captcha_" + username;
        redisTemplate.opsForValue().set(captchaKey, captchaResult.getCode(), 5, TimeUnit.MINUTES);
        
        JSONObject result = new JSONObject();
        result.put("image", "data:image/png;base64," + captchaResult.getImageBase64());
        result.put("captchaId", captchaKey);
        
        return ApiResponse.success(result);
    }
    
    /**
     * 启用双因素认证
     */
    @PostMapping("/enable2fa")
//    @CheckToken
    public ApiResponse enable2FA(@RequestBody String body) {
        JSONObject jsonObject = JSONObject.parseObject(body);
        String userId = jsonObject.getString("userId");
        
        User user = mongoTemplate.findById(userId, User.class);
        if (user == null) {
            return ApiResponse.failure("用户不存在");
        }
        
        // 如果用户已经有密钥且已启用2FA，直接返回现有信息
        if (user.isTwoFactorEnabled()&& user.getSecretKey() != null && !user.getSecretKey().isEmpty()) {
            JSONObject result = new JSONObject();
            result.put("secretKey", user.getSecretKey());
            result.put("qrCodeUrl", GoogleAuthenticatorUtil.getQRCodeUrl(user.getSecretKey(), user.getUsername(), "FreeMix"));
            return ApiResponse.success(result);
        }
        
        // 生成新的密钥
        String secretKey = GoogleAuthenticatorUtil.generateSecretKey(user.getUsername()).substring(0,16);
//        user.setSecretKey(secretKey);
        // 注意：此时不要立即设置为启用，等验证通过后再启用
        
        // 更新数据库
//        mongoTemplate.save(user);
        
        // 返回密钥和二维码URL
        JSONObject result = new JSONObject();
        result.put("secretKey", secretKey);
        String freeMix = GoogleAuthenticatorUtil.getQRCodeUrl(secretKey, user.getUsername(), "FreeMix");
//        log.info("freeMix: {}", freeMix);
//        String string = freeMix.substring(0, 60);
        result.put("qrCodeUrl",freeMix );
        
        return ApiResponse.success(result);
    }
    
    /**
     * 验证并启用双因素认证
     */
    @PostMapping("/verify2fa")
//    @CheckToken
    public ApiResponse verify2FA(@RequestBody String body) {
        JSONObject jsonObject = JSONObject.parseObject(body);
        String userId = jsonObject.getString("userId");
        String secretKey = jsonObject.getString("secretKey");
        long totpCode = jsonObject.getLongValue("totpCode");
        
        User user = mongoTemplate.findById(userId, User.class);
        if (user == null) {
            return ApiResponse.failure("用户不存在");
        }
        user.setSecretKey(secretKey);
        // 验证TOTP代码
        if (GoogleAuthenticatorUtil.verifyCode(user.getSecretKey(), totpCode)) {
            user.setTwoFactorEnabled(true);

            mongoTemplate.save(user);
            return ApiResponse.success(user, "双因素认证已启用");
        } else {
            return ApiResponse.failure("验证码错误");
        }
    }
    
    /**
     * 禁用双因素认证
     */
    @PostMapping("/disable2fa")
    @CheckToken
    public ApiResponse disable2FA(@RequestBody String body) {
        JSONObject jsonObject = JSONObject.parseObject(body);
        String userId = jsonObject.getString("userId");
        
        User user = mongoTemplate.findById(userId, User.class);
        if (user == null) {
            return ApiResponse.failure("用户不存在");
        }
        
        user.setTwoFactorEnabled(false);
        // 注意：保留secretKey以便用户以后重新启用
        mongoTemplate.save(user);
        
        return ApiResponse.success(null, "双因素认证已禁用");
    }
    
    @GetMapping("/getOwerList")
    @CheckToken
    public ApiResponse getOwerList(){
        List<User> users = mongoTemplate.find(new Query(), User.class);
        Set<JSONObject> collect = users.stream().map(e -> {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("value", e.getUsername());
            jsonObject.put("text", e.getUsername());
            return jsonObject;
        }).collect(Collectors.toSet());

        return ApiResponse.success(collect);
    }
    @PostMapping("/edituserinfo")
    @CheckToken
    public ApiResponse editUserInfo(@RequestBody  String body){
        User user = JSONObject.parseObject(body, User.class);
        mongoTemplate.save(user);

        return ApiResponse.success(user);
    }

}