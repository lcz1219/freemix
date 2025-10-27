package com.freemix.freemix.configurer;

import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Component
public class CustomOAuth2AuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    private final DefaultOAuth2AuthorizationRequestResolver defaultResolver;

    public CustomOAuth2AuthorizationRequestResolver(ClientRegistrationRepository clientRegistrationRepository) {
        this.defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(
                clientRegistrationRepository, "/oauth2/authorization");
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        OAuth2AuthorizationRequest authorizationRequest = defaultResolver.resolve(request);
        return authorizationRequest != null ? customizeAuthorizationRequest(authorizationRequest) : null;
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        OAuth2AuthorizationRequest authorizationRequest = defaultResolver.resolve(request, clientRegistrationId);
        return authorizationRequest != null ? customizeAuthorizationRequest(authorizationRequest) : null;
    }

    public OAuth2AuthorizationRequest customizeAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest) {
        Map<String, Object> additionalParameters = new HashMap<>(authorizationRequest.getAdditionalParameters());
        // GitHub OAuth2 支持的参数:
        // login: 提供一个特定的登录账户用于预填充
        // allow_signup: 控制是否向未认证用户提供注册选项 (true/false)
        // GitHub 不支持标准的 OpenID Connect prompt 参数
//        additionalParameters.put("prompt", "login"); // 强制用户在GitHub端重新登录[7](@ref)
        // 强制重新登录的关键参数
        additionalParameters.put("login", ""); // 空字符串强制显示登录界面
        // 对于GitHub，有时也可能需要尝试使用其特定参数，但建议先尝试标准的 `prompt=login`
        // additionalParameters.put("login", ""); // 可选：尝试使用空值触发账号输入

        // 您之前可能已经添加的参数，比如允许注册
        additionalParameters.put("allow_signup", "true");

        // 生成并设置唯一的state参数，防止CSRF攻击
        String state = generateSecureRandomState();
        // ... (将state存入session的逻辑)

        return OAuth2AuthorizationRequest.from(authorizationRequest)
                .additionalParameters(additionalParameters)
                .state(state) // 设置自定义的state
                .build();
    }
    private String generateSecureRandomState() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] buffer = new byte[32];
        secureRandom.nextBytes(buffer);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buffer);
    }
}