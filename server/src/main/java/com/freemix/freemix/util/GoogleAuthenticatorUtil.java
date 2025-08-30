package com.freemix.freemix.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Random;

public class GoogleAuthenticatorUtil {
    
    // 符合TOTP标准的密钥长度（16字节 = 128位）
    private static final int SECRET_SIZE = 16;
    private static final int VALIDITY_PERIOD = 30;
    private static final int WINDOW_SIZE = 3;
    
    private static final String ALGORITHM = "HmacSHA1";
    
    /**
     * 生成用户密钥
     * @param username 用户名
     * @return Base32编码的密钥，符合TOTP标准
     */
    public static String generateSecretKey(String username) {
        try {
            // 创建平台无关的安全随机数生成器
            SecureRandom secureRandom = SecureRandom.getInstanceStrong();
            byte[] buffer = new byte[SECRET_SIZE];
            secureRandom.nextBytes(buffer);
            
            // 将字节数组转换为标准的Base32编码，包含填充字符
            return base32Encode(buffer);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("无法生成安全随机数", e);
        }
    }
    
    /**
     * 验证一次性密码
     * @param secret 密钥
     * @param token 一次性密码
     * @return 是否验证成功
     */
    public static boolean verifyCode(String secret, long token) {
        // 获取当前时间窗口
        long currentTimeSeconds = System.currentTimeMillis() / 1000;
        long timeWindow = currentTimeSeconds / VALIDITY_PERIOD;
        
        // 在时间窗口前后验证
        for (int i = -WINDOW_SIZE; i <= WINDOW_SIZE; i++) {
            long hash = calculateCode(secret, timeWindow + i);
            if (hash == token) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 计算基于时间的一次性密码
     * @param secret 密钥
     * @param time 时间窗口
     * @return 一次性密码
     */
    private static long calculateCode(String secret, long time) {
        byte[] decodedKey = base32Decode(secret);
        
        // 将时间转换为字节数组
        byte[] data = new byte[8];
        long value = time;
        for (int i = 8; i-- > 0; value >>>= 8) {
            data[i] = (byte) value;
        }
        
        // 使用HMAC-SHA1算法计算哈希值
        SecretKeySpec signKey = new SecretKeySpec(decodedKey, ALGORITHM);
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(signKey);
            byte[] hash = mac.doFinal(data);
            
            // 动态截断哈希值以获得6位数字
            int offset = hash[hash.length - 1] & 0xF;
            long truncatedHash = 0;
            for (int i = 0; i < 4; ++i) {
                truncatedHash <<= 8;
                truncatedHash |= (hash[offset + i] & 0xFF);
            }
            truncatedHash &= 0x7FFFFFFF;
            truncatedHash %= 1000000;
            
            return truncatedHash;
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("计算一次性密码时出错", e);
        }
    }
    
    /**
     * Base32编码，符合RFC 4648标准
     * @param bytes 字节数组
     * @return 标准的Base32编码字符串，包含填充字符
     */
    private static String base32Encode(byte[] bytes) {
        char[] digits = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567".toCharArray();
        int mask = 0x1F;
        int shift = 0;
        int bits = 0;
        StringBuilder encoded = new StringBuilder();
        
        for (int i = 0; i < bytes.length; i++) {
            bits |= (bytes[i] & 0xFF) << shift;
            shift += 8;
            
            while (shift >= 5) {
                encoded.append(digits[bits & mask]);
                bits >>= 5;
                shift -= 5;
            }
        }
        
        // 添加最后一个片段（如果有）
        if (shift > 0) {
            encoded.append(digits[bits & mask]);
        }
        
//        // 添加填充字符，确保输出长度是8的倍数
//        int paddingLength = (8 - encoded.length() % 8) % 8;
//        for (int i = 0; i < paddingLength; i++) {
//            encoded.append('=');
//        }
        
        return encoded.toString();
    }
    
    /**
     * Base32解码
     * @param secret Base32编码的密钥
     * @return 字节数组
     */
    private static byte[] base32Decode(String secret) {
        String base32Chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
        int[] lookup = new int[256];
        Arrays.fill(lookup, -1);
        
        for (int i = 0; i < base32Chars.length(); i++) {
            lookup[base32Chars.charAt(i)] = i;
        }
        
        byte[] bytes = new byte[secret.length() * 5 / 8];
        int buffer = 0;
        int bufferSize = 0;
        int index = 0;
        
        for (int i = 0; i < secret.length(); i++) {
            int c = secret.charAt(i);
            if (c >= lookup.length || lookup[c] == -1) {
                continue;
            }
            
            buffer <<= 5;
            buffer |= lookup[c];
            bufferSize += 5;
            
            if (bufferSize >= 8) {
                bytes[index++] = (byte) (buffer >> (bufferSize - 8));
                bufferSize -= 8;
            }
        }
        
        return Arrays.copyOf(bytes, index);
    }
    
    /**
     * 生成二维码内容URL，符合Google Authenticator标准
     * @param secret 密钥
     * @param username 用户名
     * @param issuer 发行方
     * @return Google Authenticator二维码URL
     */
    public static String getQRCodeUrl(String secret, String username, String issuer) {
        // 移除Base32编码中的填充字符，因为Google Authenticator不支持
        String secretWithoutPadding = secret.replaceAll("=", "");
        
        // 对URL参数进行编码，确保特殊字符被正确处理
        String encodedIssuer = java.net.URLEncoder.encode(issuer, java.nio.charset.StandardCharsets.UTF_8);
        String encodedUsername = java.net.URLEncoder.encode(username, java.nio.charset.StandardCharsets.UTF_8);
        
        // 构建标准的TOTP URL格式
//        String format = "otpauth://totp/%s:%s?secret=%s&issuer=%s&algorithm=SHA1&digits=6&period=30";
        String format = "otpauth://totp/%s:%s?secret=%s";
        return String.format(format, encodedIssuer, encodedUsername, secretWithoutPadding, encodedIssuer);
    }
}