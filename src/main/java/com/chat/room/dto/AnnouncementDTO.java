package com.chat.room.dto;

import com.chat.room.entity.Announcement;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnnouncementDTO {

    private Long id;
    private String title;
    private String content;
    private String type;
    private String priority;
    private Long authorId;
    private String authorName;
    private Boolean isPinned;
    private Boolean isPublished;
    private LocalDateTime publishAt;
    private LocalDateTime expireAt;
    private Integer viewCount;
    private Long readCount;
    private Boolean hasRead;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AnnouncementDTO fromEntity(Announcement announcement) {
        AnnouncementDTO dto = new AnnouncementDTO();
        dto.setId(announcement.getId());
        dto.setTitle(announcement.getTitle());
        dto.setContent(announcement.getContent());
        dto.setType(announcement.getType() != null ? announcement.getType().name() : null);
        dto.setPriority(announcement.getPriority() != null ? announcement.getPriority().name() : null);
        dto.setAuthorId(announcement.getAuthorId());
        dto.setIsPinned(announcement.getIsPinned());
        dto.setIsPublished(announcement.getIsPublished());
        dto.setPublishAt(announcement.getPublishAt());
        dto.setExpireAt(announcement.getExpireAt());
        dto.setViewCount(announcement.getViewCount());
        dto.setCreatedAt(announcement.getCreatedAt());
        dto.setUpdatedAt(announcement.getUpdatedAt());
        return dto;
    }
}
