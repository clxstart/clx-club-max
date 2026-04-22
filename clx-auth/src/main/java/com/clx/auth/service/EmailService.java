package com.clx.auth.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * 邮件服务。
 *
 * <p>封装邮件发送功能，用于验证码、密码重置等场景。
 *
 * <p>使用 Spring JavaMail 发送邮件，配置在 application.yml 中。
 */
@Slf4j
@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from:noreply@clx.com}")
    private String from;

    @Value("${spring.application.name:CLX}")
    private String appName;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * 发送验证码邮件。
     */
    public void sendVerificationCode(String to, String code, String codeType) {
        String subject = String.format("[%s] 邮箱验证码", appName);
        String content = buildVerificationContent(code, codeType);
        sendEmail(to, subject, content);
    }

    /**
     * 发送密码重置邮件。
     */
    public void sendPasswordResetCode(String to, String code) {
        String subject = String.format("[%s] 密码重置", appName);
        String content = buildPasswordResetContent(code);
        sendEmail(to, subject, content);
    }

    /**
     * 构建验证码邮件内容。
     */
    private String buildVerificationContent(String code, String codeType) {
        return "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"></head><body>" +
               "<div style=\"font-family:Arial,sans-serif;max-width:600px;margin:0 auto;padding:20px;\">" +
               "<h2 style=\"color:#4CAF50;\">" + codeType + "</h2>" +
               "<p>您的验证码是：<strong style=\"font-size:24px;letter-spacing:5px;\">" + code + "</strong></p>" +
               "<p>验证码有效期为 5 分钟，请尽快完成验证。</p>" +
               "<p style=\"color:#666;font-size:12px;\">此邮件由系统自动发送，请勿回复</p>" +
               "</div></body></html>";
    }

    /**
     * 构建密码重置邮件内容。
     */
    private String buildPasswordResetContent(String code) {
        return "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"></head><body>" +
               "<div style=\"font-family:Arial,sans-serif;max-width:600px;margin:0 auto;padding:20px;\">" +
               "<h2 style=\"color:#FF5722;\">密码重置</h2>" +
               "<p>您的重置验证码是：<strong style=\"font-size:24px;letter-spacing:5px;\">" + code + "</strong></p>" +
               "<p>验证码有效期为 30 分钟，请尽快完成密码重置。</p>" +
               "<p style=\"color:#666;font-size:12px;\">此邮件由系统自动发送，请勿回复</p>" +
               "</div></body></html>";
    }

    /**
     * 发送邮件。
     */
    private void sendEmail(String to, String subject, String content) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);

            mailSender.send(mimeMessage);
            log.info("邮件发送成功: to={}, subject={}", to, subject);
        } catch (MessagingException e) {
            log.error("邮件发送失败: to={}, subject={}", to, subject, e);
            throw new RuntimeException("邮件发送服务异常", e);
        }
    }
}
