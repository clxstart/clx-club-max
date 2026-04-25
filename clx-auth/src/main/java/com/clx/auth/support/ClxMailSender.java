package com.clx.auth.support;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

/**
 * CLX 邮件发送器
 * <p>
 * 封装 JavaMail，统一发送验证码、密码重置等邮件
 * 注意：类名使用 Clx 前缀，避免与 Spring Boot 自动配置的 mailSender bean 冲突
 */
@Slf4j
@Component("clxMailSender")
public class ClxMailSender {

    /** Spring 邮件发送器（自动注入 SMTP 配置） */
    private final JavaMailSender mailSender;

    /** 发件人地址，默认 noreply@clx.com */
    @Value("${spring.mail.from:noreply@clx.com}")
    private String from;

    /** 应用名称，用于邮件标题前缀 */
    @Value("${spring.application.name:CLX}")
    private String appName;

    public ClxMailSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /** 发送验证码邮件（注册、绑定邮箱等场景） */
    public void sendCode(String to, String code, String type) {
        String subject = "[" + appName + "] 验证码";
        String content = "<p>您的" + type + "验证码：<strong style='font-size:24px'>" + code + "</strong></p>" +
                "<p>有效期 5 分钟</p>" +
                "<p style='color:#666;font-size:12px'>请勿回复</p>";
        send(to, subject, content);
    }

    /** 发送密码重置邮件（30分钟有效） */
    public void sendResetCode(String to, String code) {
        String subject = "[" + appName + "] 密码重置";
        String content = "<p>重置验证码：<strong style='font-size:24px'>" + code + "</strong></p>" +
                "<p>有效期 30 分钟</p>" +
                "<p style='color:#666;font-size:12px'>请勿回复</p>";
        send(to, subject, content);
    }

    /** 底层发送：构造 MIME 邮件 + 异常处理 */
    private void send(String to, String subject, String content) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);  // true = HTML 格式
            mailSender.send(msg);
            log.info("邮件发送成功: to={}", to);
        } catch (MessagingException e) {
            log.error("邮件发送失败: to={}", to, e);
            throw new RuntimeException("邮件发送失败", e);
        }
    }
}