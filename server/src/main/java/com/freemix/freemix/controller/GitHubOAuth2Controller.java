package com.freemix.freemix.controller;

import com.freemix.freemix.enetiy.User;
import com.freemix.freemix.service.UserService;
import com.freemix.freemix.util.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
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

@RestController
public class GitHubOAuth2Controller extends BaseController {

    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;

    @Autowired
    private UserService userService;

    /**
     * GitHub OAuth2登录成功后的回调处理
     */
    @GetMapping("/loginSuccess")
    public void loginSuccess(OAuth2AuthenticationToken authentication,
                             HttpServletRequest request,
                             HttpServletResponse response) throws IOException {
        // 获取OAuth2用户信息
        OAuth2User oAuth2User = authentication.getPrincipal();

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

        // 生成用户token并保存到数据库
        String token = generateUserToken();
        user.setToken(token);
        userService.save(user);

        // 保存用户信息到session
        HttpSession session = request.getSession();
        session.setAttribute("user", user);

        // 重定向到前端OAuth回调页面，并传递token
        response.sendRedirect("http://8.148.242.131/#/oauth/callback?token=" + token);
    }

    /**
     * GitHub OAuth2登录失败后的处理
     */
    @GetMapping("/loginFailure")
    public void loginFailure(HttpServletResponse response) throws IOException {
        // 重定向到前端登录页面，并传递错误信息
        response.sendRedirect("http://8.148.242.131/#/login?error=github_login_failed");
    }

    /**
     * 生成用户token
     * @return token字符串
     */
    private String generateUserToken() {
        // 生成随机token
        return "github_" + System.currentTimeMillis() + "_" + Math.random();
    }
}