package com.freemix.freemix.service;

public interface MailService {
    /**
     * 发送 6 位随机验证码邮件
     * @param toEmail 收件人邮箱
     * @param code 验证码
     * @return 是否发送成功
     */
    boolean sendVerificationCode(String toEmail, String code);
}
