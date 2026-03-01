package com.chat.room.service;

import com.chat.room.config.AppProperties;
import com.chat.room.entity.EmailSendLog;
import com.chat.room.exception.BusinessException;
import com.chat.room.repository.EmailSendLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final EmailSendLogRepository emailSendLogRepository;
    private final AppProperties appProperties;

    @Async
    @Transactional
    public void sendVerificationCode(String to, String code) {
        checkAndIncrementDailyLimit(to, "VERIFICATION");
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(appProperties.getEmail().getFrom());
            helper.setTo(to);
            helper.setSubject(appProperties.getEmail().getVerification().getSubject());
            
            String htmlContent = buildVerificationEmailTemplate(code);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("Verification email sent successfully to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send verification email to: {}", to, e);
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

    @Async
    @Transactional
    public void sendSimpleEmail(String to, String subject, String content) {
        checkAndIncrementDailyLimit(to, "SIMPLE");
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(appProperties.getEmail().getFrom());
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
            
            mailSender.send(message);
            log.info("Simple email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send simple email to: {}", to, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    @Async
    @Transactional
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        checkAndIncrementDailyLimit(to, "HTML");
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(appProperties.getEmail().getFrom());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("HTML email sent successfully to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send HTML email to: {}", to, e);
            throw new RuntimeException("Failed to send HTML email", e);
        }
    }

    private void checkAndIncrementDailyLimit(String email, String type) {
        if (!appProperties.getEmail().getDailyLimit().isEnabled()) {
            return;
        }

        int dailyMaxCount = appProperties.getEmail().getDailyLimit().getMaxCount();
        LocalDate today = LocalDate.now();
        int currentCount = emailSendLogRepository.getSendCountByEmailAndDate(email, today);
        
        if (currentCount >= dailyMaxCount) {
            log.warn("Email daily limit exceeded for: {} (count: {}, max: {})", 
                    email, currentCount, dailyMaxCount);
            throw new BusinessException(String.format("今日发送邮件次数已达上限(%d次)，请明天再试", dailyMaxCount));
        }

        EmailSendLog emailLog = emailSendLogRepository.findByEmailAndSendDate(email, today)
                .orElse(null);
        
        if (emailLog == null) {
            emailLog = EmailSendLog.builder()
                    .email(email)
                    .type(type)
                    .sendDate(today)
                    .sendCount(1)
                    .build();
            emailSendLogRepository.save(emailLog);
        } else {
            emailSendLogRepository.incrementSendCount(email, today);
        }
    }

    public int getRemainingDailyCount(String email) {
        if (!appProperties.getEmail().getDailyLimit().isEnabled()) {
            return -1;
        }
        
        LocalDate today = LocalDate.now();
        int currentCount = emailSendLogRepository.getSendCountByEmailAndDate(email, today);
        return Math.max(0, appProperties.getEmail().getDailyLimit().getMaxCount() - currentCount);
    }

    private String buildVerificationEmailTemplate(String code) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; }
                    .content { background-color: #f9f9f9; padding: 20px; border-radius: 5px; margin-top: 20px; }
                    .code { font-size: 32px; font-weight: bold; color: #4CAF50; text-align: center; padding: 20px; background-color: #fff; border: 2px dashed #4CAF50; border-radius: 5px; letter-spacing: 5px; }
                    .footer { text-align: center; margin-top: 20px; color: #666; font-size: 12px; }
                    .warning { color: #ff6b6b; font-size: 14px; margin-top: 15px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>聊天室 - 邮箱验证</h1>
                    </div>
                    <div class="content">
                        <p>您好！</p>
                        <p>您正在进行邮箱验证操作，请使用以下验证码完成验证：</p>
                        <div class="code">%s</div>
                        <p class="warning">验证码有效期为%d分钟，请尽快完成验证。</p>
                        <p>如果您没有进行此操作，请忽略此邮件。</p>
                    </div>
                    <div class="footer">
                        <p>此邮件由系统自动发送，请勿直接回复。</p>
                        <p>&copy; 2024 聊天室. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(code, appProperties.getEmail().getVerification().getCodeExpiration() / 60);
    }
}
