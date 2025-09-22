package com.freemix.freemix.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component // 确保这是一个由Spring管理的Bean
public class EnvironmentChecker {

    @Autowired
    private Environment env;

    public String checkEnvironment() {
        // 获取所有激活的Profile数组
        String[] activeProfiles = env.getActiveProfiles();

        // 遍历打印（调试用）
        for (String profile : activeProfiles) {
            System.out.println("激活的Profile: " + profile);
        }

        // 常见的环境判断逻辑
        if (activeProfiles.length == 0) {
            System.out.println("当前没有激活任何环境Profile。");
        } else {
            // 判断是否包含特定环境
            boolean isDev = env.acceptsProfiles("dev");
            boolean isProd = env.acceptsProfiles("prod");
            // 或者遍历 activeProfiles 数组进行判断

            if (isDev) {
                return "dev";
                // 开发环境的特定逻辑...
            } else if (isProd) {
                return "prod";
                // 生产环境的特定逻辑...
            } else {
                return "";
            }
        }
        return "";
    }
}
