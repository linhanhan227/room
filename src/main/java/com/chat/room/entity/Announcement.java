package com.chat.room.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "announcements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Announcement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 20)
    @Builder.Default
    private AnnouncementType type = AnnouncementType.NORMAL;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", length = 20)
    @Builder.Default
    private Priority priority = Priority.NORMAL;

    @Column(name = "author_id")
    private Long authorId;

    @Column(name = "is_pinned")
    @Builder.Default
    private Boolean isPinned = false;

    @Column(name = "is_published")
    @Builder.Default
    private Boolean isPublished = false;

    @Column(name = "publish_at")
    private LocalDateTime publishAt;

    @Column(name = "expire_at")
    private LocalDateTime expireAt;

    @Column(name = "view_count")
    @Builder.Default
    private Integer viewCount = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum AnnouncementType {
        NORMAL,
        IMPORTANT,
        SYSTEM,
        MAINTENANCE,
        UPDATE
    }

    public enum Priority {
        LOW,
        NORMAL,
        HIGH,
        URGENT
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
        if (viewCount == null) {
            viewCount = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
