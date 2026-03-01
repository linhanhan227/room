package com.chat.room.controller;

import com.chat.room.dto.ApiResponse;
import com.chat.room.dto.SendVerificationCodeRequest;
import com.chat.room.dto.VerifyEmailRequest;
import com.chat.room.entity.EmailVerification;
import com.chat.room.service.EmailService;
import com.chat.room.service.EmailVerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/email")
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationController {

    private final EmailVerificationService verificationService;
    private final EmailService emailService;

    @PostMapping("/verification/send")
    public ResponseEntity<ApiResponse<Void>> sendVerificationCode(
            @Valid @RequestBody SendVerificationCodeRequest request) {
        try {
            EmailVerification.VerificationType type = parseVerificationType(request.getType());
            verificationService.sendVerificationCode(request.getEmail(), type);
            return ResponseEntity.ok(ApiResponse.success("验证码发送成功", null));
        } catch (RuntimeException e) {
            log.error("Failed to send verification code: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/verification/verify")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(
            @Valid @RequestBody VerifyEmailRequest request) {
        try {
            EmailVerification.VerificationType type = parseVerificationType(request.getType());
            boolean verified = verificationService.verifyCode(request.getEmail(), request.getCode(), type);
            
            if (verified) {
                return ResponseEntity.ok(ApiResponse.success("邮箱验证成功", null));
            } else {
                return ResponseEntity.badRequest().body(ApiResponse.error("验证码无效或已过期"));
            }
        } catch (RuntimeException e) {
            log.error("Failed to verify email: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/verification/status")
    public ResponseEntity<ApiResponse<Boolean>> checkVerificationStatus(
            @RequestParam String email,
            @RequestParam String type) {
        try {
            EmailVerification.VerificationType verificationType = parseVerificationType(type);
            boolean hasValid = verificationService.hasValidVerification(email, verificationType);
            return ResponseEntity.ok(ApiResponse.success(hasValid));
        } catch (RuntimeException e) {
            log.error("Failed to check verification status: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/daily-limit")
    public ResponseEntity<ApiResponse<Integer>> getDailyLimit(@RequestParam String email) {
        int remaining = emailService.getRemainingDailyCount(email);
        return ResponseEntity.ok(ApiResponse.success(remaining));
    }

    private EmailVerification.VerificationType parseVerificationType(String type) {
        try {
            return EmailVerification.VerificationType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("无效的验证类型，可选值: REGISTER, RESET_PASSWORD, CHANGE_EMAIL");
        }
    }
}
