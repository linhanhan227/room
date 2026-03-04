package com.chat.room.service;

import com.chat.room.dto.AnnouncementDTO;
import com.chat.room.dto.AnnouncementRequest;
import com.chat.room.entity.Announcement;
import com.chat.room.entity.AnnouncementRead;
import com.chat.room.entity.User;
import com.chat.room.exception.BusinessException;
import com.chat.room.exception.ResourceNotFoundException;
import com.chat.room.repository.AnnouncementReadRepository;
import com.chat.room.repository.AnnouncementRepository;
import com.chat.room.repository.UserRepository;
import com.chat.room.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final AnnouncementReadRepository announcementReadRepository;
    private final UserRepository userRepository;

    @Transactional
    public AnnouncementDTO createAnnouncement(AnnouncementRequest request) {
        User currentUser = getCurrentUser();

        Announcement announcement = Announcement.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .type(parseType(request.getType()))
                .priority(parsePriority(request.getPriority()))
                .authorId(currentUser.getId())
                .isPinned(request.getIsPinned() != null ? request.getIsPinned() : false)
                .isPublished(request.getIsPublished() != null ? request.getIsPublished() : false)
                .publishAt(parseDateTime(request.getPublishAt()))
                .expireAt(parseDateTime(request.getExpireAt()))
                .build();

        announcement = announcementRepository.save(announcement);
        log.info("Announcement created: {} by user {}", announcement.getId(), currentUser.getId());

        return enrichAnnouncementDTO(AnnouncementDTO.fromEntity(announcement), currentUser.getId());
    }

    @Transactional
    public AnnouncementDTO updateAnnouncement(Long id, AnnouncementRequest request) {
        User currentUser = getCurrentUser();

        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("公告不存在"));

        announcement.setTitle(request.getTitle());
        announcement.setContent(request.getContent());
        if (request.getType() != null) {
            announcement.setType(parseType(request.getType()));
        }
        if (request.getPriority() != null) {
            announcement.setPriority(parsePriority(request.getPriority()));
        }
        if (request.getIsPinned() != null) {
            announcement.setIsPinned(request.getIsPinned());
        }
        if (request.getIsPublished() != null) {
            announcement.setIsPublished(request.getIsPublished());
        }
        if (request.getPublishAt() != null) {
            announcement.setPublishAt(parseDateTime(request.getPublishAt()));
        }
        if (request.getExpireAt() != null) {
            announcement.setExpireAt(parseDateTime(request.getExpireAt()));
        }

        announcement = announcementRepository.save(announcement);
        log.info("Announcement updated: {} by user {}", id, currentUser.getId());

        return enrichAnnouncementDTO(AnnouncementDTO.fromEntity(announcement), currentUser.getId());
    }

    @Transactional
    public void deleteAnnouncement(Long id) {
        if (!announcementRepository.existsById(id)) {
            throw new ResourceNotFoundException("公告不存在");
        }
        announcementReadRepository.deleteByAnnouncementId(id);
        announcementRepository.deleteById(id);
        log.info("Announcement deleted: {}", id);
    }

    @Transactional
    public AnnouncementDTO publishAnnouncement(Long id) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("公告不存在"));

        announcement.setIsPublished(true);
        announcement.setPublishAt(LocalDateTime.now());
        announcement = announcementRepository.save(announcement);

        log.info("Announcement published: {}", id);
        return enrichAnnouncementDTO(AnnouncementDTO.fromEntity(announcement), getCurrentUser().getId());
    }

    @Transactional
    public AnnouncementDTO unpublishAnnouncement(Long id) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("公告不存在"));

        announcement.setIsPublished(false);
        announcement = announcementRepository.save(announcement);

        log.info("Announcement unpublished: {}", id);
        return enrichAnnouncementDTO(AnnouncementDTO.fromEntity(announcement), getCurrentUser().getId());
    }

    @Transactional
    public AnnouncementDTO pinAnnouncement(Long id) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("公告不存在"));

        announcement.setIsPinned(true);
        announcement = announcementRepository.save(announcement);

        log.info("Announcement pinned: {}", id);
        return enrichAnnouncementDTO(AnnouncementDTO.fromEntity(announcement), getCurrentUser().getId());
    }

    @Transactional
    public AnnouncementDTO unpinAnnouncement(Long id) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("公告不存在"));

        announcement.setIsPinned(false);
        announcement = announcementRepository.save(announcement);

        log.info("Announcement unpinned: {}", id);
        return enrichAnnouncementDTO(AnnouncementDTO.fromEntity(announcement), getCurrentUser().getId());
    }

    @Transactional
    public void markAsRead(Long announcementId) {
        User currentUser = getCurrentUser();

        if (!announcementRepository.existsById(announcementId)) {
            throw new ResourceNotFoundException("公告不存在");
        }

        if (!announcementReadRepository.existsByAnnouncementIdAndUserId(announcementId, currentUser.getId())) {
            AnnouncementRead read = AnnouncementRead.builder()
                    .announcementId(announcementId)
                    .userId(currentUser.getId())
                    .build();
            announcementReadRepository.save(read);

            announcementRepository.incrementViewCount(announcementId);
        }
    }

    @Transactional
    public void markAllAsRead() {
        User currentUser = getCurrentUser();
        List<Announcement> activeAnnouncements = announcementRepository.findActiveAnnouncements(LocalDateTime.now());

        for (Announcement announcement : activeAnnouncements) {
            if (!announcementReadRepository.existsByAnnouncementIdAndUserId(announcement.getId(), currentUser.getId())) {
                AnnouncementRead read = AnnouncementRead.builder()
                        .announcementId(announcement.getId())
                        .userId(currentUser.getId())
                        .build();
                announcementReadRepository.save(read);
                announcementRepository.incrementViewCount(announcement.getId());
            }
        }
    }

    public AnnouncementDTO getAnnouncementById(Long id) {
        User currentUser = getCurrentUser();
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("公告不存在"));
        return enrichAnnouncementDTO(AnnouncementDTO.fromEntity(announcement), currentUser.getId());
    }

    public Page<AnnouncementDTO> getPublishedAnnouncements(Pageable pageable) {
        User currentUser = getCurrentUser();
        return announcementRepository.findByIsPublishedTrueOrderByIsPinnedDescPriorityDescCreatedAtDesc(pageable)
                .map(a -> enrichAnnouncementDTO(AnnouncementDTO.fromEntity(a), currentUser.getId()));
    }

    public Page<AnnouncementDTO> getActiveAnnouncements(Pageable pageable) {
        User currentUser = getCurrentUser();
        return announcementRepository.findActiveAnnouncements(LocalDateTime.now(), pageable)
                .map(a -> enrichAnnouncementDTO(AnnouncementDTO.fromEntity(a), currentUser.getId()));
    }

    public List<AnnouncementDTO> getPinnedAnnouncements() {
        User currentUser = getCurrentUser();
        return announcementRepository.findByIsPublishedTrueAndIsPinnedTrueOrderByPriorityDescCreatedAtDesc().stream()
                .map(a -> enrichAnnouncementDTO(AnnouncementDTO.fromEntity(a), currentUser.getId()))
                .collect(Collectors.toList());
    }

    public Page<AnnouncementDTO> getAllAnnouncements(Pageable pageable) {
        User currentUser = getCurrentUser();
        return announcementRepository.findAll(pageable)
                .map(a -> enrichAnnouncementDTO(AnnouncementDTO.fromEntity(a), currentUser.getId()));
    }

    public Page<AnnouncementDTO> getAnnouncementsByType(String type, Pageable pageable) {
        User currentUser = getCurrentUser();
        return announcementRepository.findByIsPublishedTrueAndTypeOrderByIsPinnedDescPriorityDescCreatedAtDesc(
                parseType(type), pageable)
                .map(a -> enrichAnnouncementDTO(AnnouncementDTO.fromEntity(a), currentUser.getId()));
    }

    public Map<String, Object> getAnnouncementStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("total", announcementRepository.count());
        stats.put("published", announcementRepository.countActiveAnnouncements(LocalDateTime.now()));
        stats.put("pinned", announcementRepository.findByIsPublishedTrueAndIsPinnedTrueOrderByPriorityDescCreatedAtDesc().size());

        return stats;
    }

    public Map<String, Object> getUserReadStatistics() {
        User currentUser = getCurrentUser();
        Map<String, Object> stats = new HashMap<>();
        
        long totalActive = announcementRepository.countActiveAnnouncements(LocalDateTime.now());
        long readCount = announcementReadRepository.countReadAnnouncementsByUserId(currentUser.getId());
        long unreadCount = totalActive - readCount;

        stats.put("totalActive", totalActive);
        stats.put("readCount", readCount);
        stats.put("unreadCount", Math.max(0, unreadCount));

        return stats;
    }

    private AnnouncementDTO enrichAnnouncementDTO(AnnouncementDTO dto, Long userId) {
        if (dto.getAuthorId() != null) {
            userRepository.findById(dto.getAuthorId())
                    .ifPresent(u -> dto.setAuthorName(u.getNickname() != null ? u.getNickname() : u.getUsername()));
        }

        dto.setReadCount(announcementReadRepository.countReadersByAnnouncementId(dto.getId()));
        dto.setHasRead(announcementReadRepository.existsByAnnouncementIdAndUserId(dto.getId(), userId));

        return dto;
    }

    private Announcement.AnnouncementType parseType(String type) {
        if (type == null || type.isEmpty()) {
            return Announcement.AnnouncementType.NORMAL;
        }
        try {
            return Announcement.AnnouncementType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("无效的公告类型: " + type);
        }
    }

    private Announcement.Priority parsePriority(String priority) {
        if (priority == null || priority.isEmpty()) {
            return Announcement.Priority.NORMAL;
        }
        try {
            return Announcement.Priority.valueOf(priority.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("无效的优先级: " + priority);
        }
    }

    private LocalDateTime parseDateTime(String dateTime) {
        if (dateTime == null || dateTime.isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException e) {
            throw new BusinessException("无效的日期时间格式: " + dateTime);
        }
    }

    private User getCurrentUser() {
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", principal.getId()));
    }
}
