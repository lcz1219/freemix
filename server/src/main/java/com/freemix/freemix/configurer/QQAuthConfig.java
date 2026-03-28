package com.freemix.freemix.configurer;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "justauth.type.qq")
public class QQAuthConfig {
    private String clientId;
    private String clientSecret;
    private String redirectUri;
}
