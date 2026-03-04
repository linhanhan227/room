package com.chat.room.repository;

import com.chat.room.entity.Announcement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    Page<Announcement> findByIsPublishedTrueOrderByIsPinnedDescPriorityDescCreatedAtDesc(Pageable pageable);

    List<Announcement> findByIsPublishedTrueAndIsPinnedTrueOrderByPriorityDescCreatedAtDesc();

    Page<Announcement> findByIsPublishedTrueAndTypeOrderByIsPinnedDescPriorityDescCreatedAtDesc(
            Announcement.AnnouncementType type, Pageable pageable);

    Page<Announcement> findByIsPublishedTrueAndPriorityOrderByIsPinnedDescCreatedAtDesc(
            Announcement.Priority priority, Pageable pageable);

    @Query("SELECT a FROM Announcement a WHERE a.isPublished = true " +
           "AND (a.expireAt IS NULL OR a.expireAt > :now) " +
           "ORDER BY a.isPinned DESC, a.priority DESC, a.createdAt DESC")
    List<Announcement> findActiveAnnouncements(LocalDateTime now);

    @Query("SELECT a FROM Announcement a WHERE a.isPublished = true " +
           "AND (a.expireAt IS NULL OR a.expireAt > :now) " +
           "ORDER BY a.isPinned DESC, a.priority DESC, a.createdAt DESC")
    Page<Announcement> findActiveAnnouncements(LocalDateTime now, Pageable pageable);

    @Modifying
    @Query("UPDATE Announcement a SET a.viewCount = a.viewCount + 1 WHERE a.id = :id")
    void incrementViewCount(Long id);

    @Query("SELECT COUNT(a) FROM Announcement a WHERE a.isPublished = true " +
           "AND (a.expireAt IS NULL OR a.expireAt > :now)")
    long countActiveAnnouncements(LocalDateTime now);

    List<Announcement> findByAuthorIdOrderByCreatedAtDesc(Long authorId);

    @Modifying
    @Query("UPDATE Announcement a SET a.isPinned = :pinned WHERE a.id = :id")
    void updatePinnedStatus(Long id, boolean pinned);

    @Modifying
    @Query("UPDATE Announcement a SET a.isPublished = true, a.publishAt = :publishAt WHERE a.id = :id")
    void publishAnnouncement(Long id, LocalDateTime publishAt);
}
