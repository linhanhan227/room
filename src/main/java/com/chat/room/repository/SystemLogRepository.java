package com.chat.room.repository;

import com.chat.room.entity.SystemLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SystemLogRepository extends JpaRepository<SystemLog, Long> {

    Page<SystemLog> findByOperatorId(Long operatorId, Pageable pageable);

    Page<SystemLog> findByTargetUserId(Long targetUserId, Pageable pageable);

    Page<SystemLog> findByAction(String action, Pageable pageable);

    @Query("SELECT sl FROM SystemLog sl WHERE sl.createdAt BETWEEN :start AND :end")
    Page<SystemLog> findByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, Pageable pageable);

    @Query("SELECT sl FROM SystemLog sl WHERE sl.level = :level")
    Page<SystemLog> findByLevel(@Param("level") SystemLog.LogLevel level, Pageable pageable);

    @Query("SELECT sl FROM SystemLog sl WHERE sl.entityType = :entityType AND sl.entityId = :entityId")
    List<SystemLog> findByEntity(@Param("entityType") String entityType, @Param("entityId") Long entityId);

    @Query("SELECT DISTINCT sl.action FROM SystemLog sl")
    List<String> findAllActions();
}
