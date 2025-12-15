package com.freemix.freemix.enetiy;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
public class User {
    String id;
    String username;
    String chinesename;
    String email;
    String password;
    String saveQuestionOne;
    String saveAnOne;
    String saveQuestionTwice;
    String saveAnTwice;
    String saveQuestionThree;
    String saveAnThree;
    String token;
    String deskToken;
    String mobileToken; // 移动端token
    String avatarUrl;
    String fashionTitle;
    String githubId;  // GitHub用户ID
    
    // Google Authenticator 双因素认证字段
    boolean twoFactorEnabled = false;  // 是否启用2FA

    public boolean isTwoFactorEnabled() {
        return twoFactorEnabled;
    }

    public void setTwoFactorEnabled(boolean twoFactorEnabled) {
        this.twoFactorEnabled = twoFactorEnabled;
    }

    String secretKey;  // Google Authenticator密钥
}