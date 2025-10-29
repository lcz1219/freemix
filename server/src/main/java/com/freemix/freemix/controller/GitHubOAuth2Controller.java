package com.freemix.freemix.controller;

import com.freemix.freemix.enetiy.User;
import com.freemix.freemix.service.UserService;
import com.freemix.freemix.util.ApiResponse;
import com.freemix.freemix.util.LoginLogUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
public class GitHubOAuth2Controller extends BaseController {

    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;

    @Autowired
    private UserService userService;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * GitHub OAuth2登录成功后的回调处理
     */
    @GetMapping("/loginSuccess")
    public void loginSuccess(OAuth2AuthenticationToken authentication,
                             HttpServletRequest request,
                             HttpServletResponse response) throws IOException {
        // 获取OAuth2用户信息
        OAuth2User oAuth2User = authentication.getPrincipal();
        log.info(oAuth2User.toString());

        // 获取用户代理信息以判断设备类型
        String userAgent = request.getHeader("User-Agent");
        String deviceType = LoginLogUtil.getDeviceType(userAgent);
        boolean isDesktop = userAgent != null && userAgent.contains("Electron");

        // 获取GitHub用户信息
        String githubId = oAuth2User.getAttribute("id").toString();
        String login = oAuth2User.getAttribute("login");
        String name = oAuth2User.getAttribute("name");
        String email = oAuth2User.getAttribute("email");

        // 查找或创建用户
        User user = userService.findByGithubId(githubId);
        if (user == null) {
            // 创建新用户
            user = new User();
            user.setGithubId(githubId);
            user.setUsername(login != null ? login : "github_" + githubId);
            user.setEmail(email);
            user.setUsername(name != null ? name : login);
            user.setChinesename(name != null ? name : login);

            // 设置默认密码（GitHub登录用户不需要密码登录）
            user.setPassword("");

            userService.save(user);
        }

        // 生成用户token
        String token = generateUserToken();

        // 根据设备类型处理token
        if (isDesktop) {
            // 桌面端：生成并保存桌面端token
            String desktopToken = generateDesktopToken();
            user.setDeskToken(desktopToken);

            // 保存桌面端token到Redis（30天有效期）
            String desktopTokenKey = "desktop_token_" + desktopToken;
            redisTemplate.opsForValue().set(desktopTokenKey, user.getId(), 30, TimeUnit.DAYS);

            // 同时设置普通token
            user.setToken(token);

            userService.save(user);

            // 保存用户信息到session
            HttpSession session = request.getSession();
            session.setAttribute("user", user);
            // 重定向到前端OAuth回调页面，并传递desktopToken
            response.sendRedirect("https://freemix.bond/#/oauth/callback?token=" + desktopToken + "&isDesktop=true&githubId=" + githubId);
        } else {
            // Web端：使用普通token
            user.setToken(token);
            userService.save(user);
            String tokenKey = user.getUsername() + "_token";
            redisTemplate.opsForValue().set(tokenKey, user.getToken(),60, TimeUnit.MINUTES);
            // 保存用户信息到session
            HttpSession session = request.getSession();
            session.setAttribute("user", user);
            // 重定向到前端OAuth回调页面，并传递token
            response.sendRedirect("https://freemix.bond/#/oauth/callback?token=" + token+"&githubId=" + githubId);
        }

    }
        

    /**
     * GitHub OAuth2登录失败后的处理
     */
    @GetMapping("/loginFailure")
    public void loginFailure(HttpServletResponse response) throws IOException {
        // 重定向到前端登录页面，并传递错误信息
        response.sendRedirect("https://freemix.bond/#/login?error=github_login_failed");
    }

    /**
     * 生成用户token
     * @return token字符串
     */
    private String generateUserToken() {
        // 生成随机token，与LoginController保持一致
        return java.util.UUID.randomUUID().toString();
    }
    
    /**
     * 生成桌面端token
     * @return desktopToken字符串
     */
    private String generateDesktopToken() {
        // 生成随机桌面端token，与LoginController保持一致
        return java.util.UUID.randomUUID().toString();
    }
}