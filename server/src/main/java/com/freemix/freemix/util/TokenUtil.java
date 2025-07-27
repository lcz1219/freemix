package com.freemix.freemix.util;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;

public class TokenUtil {
    public static SecretKey generateKey() {
        return Keys.secretKeyFor(SignatureAlgorithm.HS256); // 自动生成满足HS256要求的密钥
    }
}
