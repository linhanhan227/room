package com.chat.room.service;

import com.chat.room.config.AppProperties;
import com.chat.room.entity.EmailVerification;
import com.chat.room.entity.User;
import com.chat.room.exception.BusinessException;
import com.chat.room.repository.EmailVerificationRepository;
import com.chat.room.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationService {

    private final EmailVerificationRepository verificationRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final AppProperties appProperties;

    private final SecureRandom random = new SecureRandom();

    @Transactional
    public void sendVerificationCode(String email, EmailVerification.VerificationType type) {
        validateEmailRequest(email, type);

        String code = generateCode();
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(appProperties.getEmail().getVerification().getCodeExpiration());

        EmailVerification verification = EmailVerification.builder()
                .email(email)
                .code(code)
                .type(type)
                .expiresAt(expiresAt)
                .used(false)
                .build();

        verificationRepository.save(verification);
        emailService.sendVerificationCode(email, code);

        log.info("Verification code sent to email: {} for type: {}", email, type);
    }

    @Transactional
    public boolean verifyCode(String email, String code, EmailVerification.VerificationType type) {
        Optional<EmailVerification> verificationOpt = verificationRepository
                .findByEmailAndCodeAndTypeAndUsedFalse(email, code, type);

        if (verificationOpt.isEmpty()) {
            log.warn("Verification code not found or already used for email: {}", email);
            return false;
        }

        EmailVerification verification = verificationOpt.get();

        if (!verification.isValid()) {
            log.warn("Verification code expired for email: {}", email);
            return false;
        }

        verification.setUsed(true);
        verificationRepository.save(verification);

        log.info("Email verified successfully: {} for type: {}", email, type);
        return true;
    }

    @Transactional
    public void markEmailAsVerified(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            log.info("Email marked as verified for user: {}", user.getUsername());
        }
    }

    @Transactional
    public void cleanupExpiredVerifications() {
        verificationRepository.deleteExpiredVerifications(LocalDateTime.now());
        log.debug("Expired email verifications cleaned up");
    }

    public boolean hasValidVerification(String email, EmailVerification.VerificationType type) {
        return verificationRepository.existsByEmailAndTypeAndUsedFalseAndExpiresAtAfter(
                email, type, LocalDateTime.now());
    }

    public Optional<EmailVerification> getLatestVerification(String email, EmailVerification.VerificationType type) {
        return verificationRepository.findTopByEmailAndTypeAndUsedFalseOrderByCreatedAtDesc(email, type);
    }

    private void validateEmailRequest(String email, EmailVerification.VerificationType type) {
        if (type == EmailVerification.VerificationType.REGISTER) {
            if (userRepository.existsByEmail(email)) {
                throw new BusinessException("该邮箱已被注册");
            }
        }

        int maxSendPerHour = appProperties.getEmail().getVerification().getMaxSendPerHour();
        long recentCount = verificationRepository.countByEmailAndCreatedAtAfter(
                email, LocalDateTime.now().minusHours(1));
        if (recentCount >= maxSendPerHour) {
            throw new BusinessException("发送验证码次数过多，请1小时后再试");
        }

        Optional<EmailVerification> lastVerification = verificationRepository
                .findTopByEmailAndTypeAndUsedFalseOrderByCreatedAtDesc(email, type);
        
        int sendIntervalSeconds = appProperties.getEmail().getVerification().getSendIntervalSeconds();
        if (lastVerification.isPresent()) {
            LocalDateTime lastSentTime = lastVerification.get().getCreatedAt();
            long secondsSinceLastSent = java.time.Duration.between(
                    lastSentTime, LocalDateTime.now()).getSeconds();
            
            if (secondsSinceLastSent < sendIntervalSeconds) {
                long waitSeconds = sendIntervalSeconds - secondsSinceLastSent;
                throw new BusinessException("请等待" + waitSeconds + "秒后再发送验证码");
            }
        }
    }

    private String generateCode() {
        int codeLength = appProperties.getEmail().getVerification().getCodeLength();
        int max = (int) Math.pow(10, codeLength) - 1;
        int min = (int) Math.pow(10, codeLength - 1);
        int code = random.nextInt(max - min + 1) + min;
        return String.valueOf(code);
    }
}
