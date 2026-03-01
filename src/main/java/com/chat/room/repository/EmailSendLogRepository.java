package com.chat.room.repository;

import com.chat.room.entity.EmailSendLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface EmailSendLogRepository extends JpaRepository<EmailSendLog, Long> {

    Optional<EmailSendLog> findByEmailAndSendDate(String email, LocalDate sendDate);

    @Modifying
    @Query("UPDATE EmailSendLog e SET e.sendCount = e.sendCount + 1 WHERE e.email = :email AND e.sendDate = :sendDate")
    void incrementSendCount(String email, LocalDate sendDate);

    @Query("SELECT COALESCE(SUM(e.sendCount), 0) FROM EmailSendLog e WHERE e.email = :email AND e.sendDate = :sendDate")
    int getSendCountByEmailAndDate(String email, LocalDate sendDate);

    @Modifying
    @Query("DELETE FROM EmailSendLog e WHERE e.sendDate < :beforeDate")
    void deleteOldLogs(LocalDate beforeDate);
}
