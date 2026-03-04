package com.chat.room.repository;

import com.chat.room.entity.AnnouncementRead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnnouncementReadRepository extends JpaRepository<AnnouncementRead, Long> {

    Optional<AnnouncementRead> findByAnnouncementIdAndUserId(Long announcementId, Long userId);

    boolean existsByAnnouncementIdAndUserId(Long announcementId, Long userId);

    List<AnnouncementRead> findByUserIdOrderByReadAtDesc(Long userId);

    @Query("SELECT COUNT(ar) FROM AnnouncementRead ar WHERE ar.announcementId = :announcementId")
    long countReadersByAnnouncementId(Long announcementId);

    @Query("SELECT COUNT(DISTINCT ar.announcementId) FROM AnnouncementRead ar WHERE ar.userId = :userId")
    long countReadAnnouncementsByUserId(Long userId);

    void deleteByAnnouncementId(Long announcementId);
}
