package com.chat.room.repository;

import com.chat.room.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    Page<Message> findByRoomIdOrderByCreatedAtDesc(Long roomId, Pageable pageable);

    List<Message> findByRoomIdAndCreatedAtAfterOrderByCreatedAtAsc(Long roomId, LocalDateTime after);

    @Query("SELECT m FROM Message m WHERE m.room.id = :roomId AND m.type != 'SYSTEM' ORDER BY m.createdAt DESC")
    List<Message> findRecentMessages(@Param("roomId") Long roomId, Pageable pageable);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.room.id = :roomId")
    Long countByRoomId(@Param("roomId") Long roomId);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.room.id = :roomId AND m.sender.id = :userId")
    Long countByRoomIdAndSenderId(@Param("roomId") Long roomId, @Param("userId") Long userId);

    @Query("SELECT m FROM Message m WHERE m.room.id = :roomId AND m.content LIKE %:keyword%")
    Page<Message> searchByContent(@Param("roomId") Long roomId, @Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.createdAt >= :since")
    Long countMessagesSince(@Param("since") LocalDateTime since);

    void deleteByRoomId(Long roomId);
}
