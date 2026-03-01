package com.chat.room.repository;

import com.chat.room.entity.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {

    Optional<EmailVerification> findByEmailAndCodeAndTypeAndUsedFalse(
            String email, String code, EmailVerification.VerificationType type);

    Optional<EmailVerification> findTopByEmailAndTypeAndUsedFalseOrderByCreatedAtDesc(
            String email, EmailVerification.VerificationType type);

    @Modifying
    @Query("UPDATE EmailVerification ev SET ev.used = true WHERE ev.email = :email AND ev.type = :type")
    void markAllAsUsed(String email, EmailVerification.VerificationType type);

    @Modifying
    @Query("DELETE FROM EmailVerification ev WHERE ev.expiresAt < :now")
    void deleteExpiredVerifications(LocalDateTime now);

    boolean existsByEmailAndTypeAndUsedFalseAndExpiresAtAfter(
            String email, EmailVerification.VerificationType type, LocalDateTime now);

    long countByEmailAndCreatedAtAfter(String email, LocalDateTime since);
}
