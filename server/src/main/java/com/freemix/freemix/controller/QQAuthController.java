package com.freemix.freemix.controller;

import com.alibaba.fastjson2.JSONObject;
import com.freemix.freemix.configurer.QQAuthConfig;
import com.freemix.freemix.enetiy.User;
import com.freemix.freemix.service.UserService;
import com.freemix.freemix.util.LoginLogUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthResponse;
import me.zhyd.oauth.model.AuthUser;
import me.zhyd.oauth.request.AuthQqRequest;
import me.zhyd.oauth.request.AuthRequest;
import me.zhyd.oauth.utils.AuthStateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/auth/qq")
public class QQAuthController extends BaseController {

    @Autowired
    private QQAuthConfig qqAuthConfig;

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private com.freemix.freemix.service.AchievementService achievementService;

    private AuthRequest getAuthRequest() {
        return new AuthQqRequest(AuthConfig.builder()
                .clientId(qqAuthConfig.getClientId())
                .clientSecret(qqAuthConfig.getClientSecret())
                .redirectUri(qqAuthConfig.getRedirectUri())
                .build());
    }

    @GetMapping("/render")
    public void renderAuth(HttpServletResponse response) throws IOException {
        AuthRequest authRequest = getAuthRequest();
        String authorizeUrl = authRequest.authorize(AuthStateUtils.createState());
        log.info("Redirecting to QQ authorize URL: {}", authorizeUrl);
        response.sendRedirect(authorizeUrl);
    }
   

    @GetMapping("/callback")
    public void login(AuthCallback callback, HttpServletRequest request, HttpServletResponse response) throws IOException {
        AuthRequest authRequest = getAuthRequest();
        AuthResponse<AuthUser> authResponse = authRequest.login(callback);
        log.info("QQ登录响应: {}", JSONObject.toJSONString(authResponse));

        if (authResponse.ok()) {
            AuthUser authUser = authResponse.getData();
            String qqOpenId = authUser.getUuid();

            // 查找或创建用户
            User user = userService.findByQqOpenId(qqOpenId);
            if (user == null) {
                user = new User();
                user.setQqOpenId(qqOpenId);
                user.setUsername(authUser.getNickname());
                user.setChinesename(authUser.getNickname());
                user.setAvatarUrl(authUser.getAvatar());
                user.setPassword(""); // 第三方登录不需要密码
                log.info("QQ登录新用户啊 {}", user);

                userService.save(user);
            }

            // 生成 Token
            String token = java.util.UUID.randomUUID().toString();
            user.setToken(token);
            userService.save(user);

            // 获取请求信息
            String userAgent = request.getHeader("User-Agent");
            boolean isDesktop = userAgent != null && userAgent.contains("Electron");
            boolean isMobile = isMobileDevice(request);

            // 设置 Token 过期时间逻辑
            if (isDesktop) {
                // 桌面端 30 天免登，Key 格式需与 CheckAspect 匹配
                log.info("桌面端 QQ 登录，设置 30 天 Token: {}", user.getUsername());
                String desktopTokenKey = "desktop_token_" + token;
                redisTemplate.opsForValue().set(desktopTokenKey, user.getId(), 30, TimeUnit.DAYS);
                user.setDeskToken(token);
                userService.save(user);
            } else if (isMobile) {
                // 移动端 不退出不登录
                log.info("移动端 QQ 登录，设置长效 Token: {}", user.getUsername());
                user.setMobileToken(token);
                userService.save(user);
            } else {
                // 普通 Web 端 60 分钟
                log.info("Web 端 QQ 登录，设置 60 分钟 Token: {}", user.getUsername());
                String webTokenKey = user.getUsername() + "_token";
                redisTemplate.opsForValue().set(webTokenKey, token, 60, TimeUnit.MINUTES);
            }

            // 保存到 Session
            HttpSession session = request.getSession();
            session.setAttribute("user", user);

            // 触发成就
            try {
                achievementService.checkAndUnlock(user.getUsername(), "LOGIN", null);
            } catch (Exception e) {
                log.error("触发成就失败", e);
            }

            log.info("QQ登录环境判断: isDesktop={}, isMobile={}", isDesktop, isMobile);

            // 重定向逻辑
            String redirectUrl;
            if (isMobile) {
                // 移动端 App 使用 Custom URL Scheme 唤回
                redirectUrl = "mobile.freemix.app://oauth/callback?token=" + token + "&qqOpenId=" + qqOpenId;
                log.info("移动端 App 唤回跳转: {}", redirectUrl);
            } else {
                // Web 端或桌面端跳转
                String baseUrl = environmentChecker.isProd() ? "https://freemix.bond" : "http://localhost:5173";
                // 统一桌面端 OAuth 回调参数：如果是桌面端，token 传的就是 desktopToken
                redirectUrl = baseUrl + "/#/oauth/callback?token=" + token + "&qqOpenId=" + qqOpenId;
                if (isDesktop) {
                    redirectUrl += "&isDesktop=true";
                }
                log.info("Web/桌面端跳转: {}", redirectUrl);
            }

            response.sendRedirect(redirectUrl);
        } else {
            String baseUrl = environmentChecker.isProd() ? "https://freemix.bond" : "http://localhost:5173";
            response.sendRedirect(baseUrl + "/#/login?error=qq_login_failed");
        }
    }
}
