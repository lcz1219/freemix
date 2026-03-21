package com.freemix.freemix.service.impl;

import com.freemix.freemix.service.MailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Slf4j
@Service
public class MailServiceImpl implements MailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public boolean sendVerificationCode(String toEmail, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "Freemix系统助手");
            helper.setTo(toEmail);
            helper.setSubject("您的验证码");

            String htmlContent = "<div style='font-family: Arial, sans-serif; padding: 20px; max-width: 600px; margin: 0 auto; border: 1px solid #eee; border-radius: 5px;'>" +
                    "<h2 style='color: #333;'>Freemix 验证码</h2>" +
                    "<p>尊敬的用户，您好：</p>" +
                    "<p>您的验证码为：</p>" +
                    "<div style='background: #f5f5f5; padding: 15px; font-size: 24px; font-weight: bold; text-align: center; letter-spacing: 5px; color: #007bff;'>" + code + "</div>" +
                    "<p style='color: #666;'>该验证码 <strong>5分钟内</strong> 有效，请勿泄露。</p>" +
                    "<p>如非本人操作，请忽略此邮件。</p>" +
                    "<hr style='border: none; border-top: 1px solid #eee;'>" +
                    "<p style='color: #999; font-size: 12px;'>Freemix 团队 | 自动发送，请勿回复</p>" +
                    "</div>";

            helper.setText(htmlContent, true);
            mailSender.send(message);
            log.info("成功发送验证码邮件至: {}", toEmail);
            return true;
        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("发送验证码邮件失败: {}", e.getMessage());
            return false;
        }
    }
}
