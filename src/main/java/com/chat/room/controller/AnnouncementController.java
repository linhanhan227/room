package com.chat.room.controller;

import com.chat.room.dto.AnnouncementDTO;
import com.chat.room.dto.AnnouncementRequest;
import com.chat.room.dto.ApiResponse;
import com.chat.room.service.AnnouncementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/announcements")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<AnnouncementDTO>> createAnnouncement(
            @Valid @RequestBody AnnouncementRequest request) {
        AnnouncementDTO announcement = announcementService.createAnnouncement(request);
        return ResponseEntity.ok(ApiResponse.success("公告创建成功", announcement));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AnnouncementDTO>> updateAnnouncement(
            @PathVariable Long id,
            @Valid @RequestBody AnnouncementRequest request) {
        AnnouncementDTO announcement = announcementService.updateAnnouncement(id, request);
        return ResponseEntity.ok(ApiResponse.success("公告更新成功", announcement));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAnnouncement(@PathVariable Long id) {
        announcementService.deleteAnnouncement(id);
        return ResponseEntity.ok(ApiResponse.success("公告删除成功", null));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/publish")
    public ResponseEntity<ApiResponse<AnnouncementDTO>> publishAnnouncement(@PathVariable Long id) {
        AnnouncementDTO announcement = announcementService.publishAnnouncement(id);
        return ResponseEntity.ok(ApiResponse.success("公告发布成功", announcement));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/unpublish")
    public ResponseEntity<ApiResponse<AnnouncementDTO>> unpublishAnnouncement(@PathVariable Long id) {
        AnnouncementDTO announcement = announcementService.unpublishAnnouncement(id);
        return ResponseEntity.ok(ApiResponse.success("公告已取消发布", announcement));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/pin")
    public ResponseEntity<ApiResponse<AnnouncementDTO>> pinAnnouncement(@PathVariable Long id) {
        AnnouncementDTO announcement = announcementService.pinAnnouncement(id);
        return ResponseEntity.ok(ApiResponse.success("公告置顶成功", announcement));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/unpin")
    public ResponseEntity<ApiResponse<AnnouncementDTO>> unpinAnnouncement(@PathVariable Long id) {
        AnnouncementDTO announcement = announcementService.unpinAnnouncement(id);
        return ResponseEntity.ok(ApiResponse.success("公告取消置顶成功", announcement));
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable Long id) {
        announcementService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.success("已标记为已读", null));
    }

    @PostMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead() {
        announcementService.markAllAsRead();
        return ResponseEntity.ok(ApiResponse.success("全部公告已标记为已读", null));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AnnouncementDTO>> getAnnouncementById(@PathVariable Long id) {
        AnnouncementDTO announcement = announcementService.getAnnouncementById(id);
        return ResponseEntity.ok(ApiResponse.success(announcement));
    }

    @GetMapping("/published")
    public ResponseEntity<ApiResponse<Page<AnnouncementDTO>>> getPublishedAnnouncements(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<AnnouncementDTO> announcements = announcementService.getPublishedAnnouncements(pageable);
        return ResponseEntity.ok(ApiResponse.success(announcements));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<Page<AnnouncementDTO>>> getActiveAnnouncements(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<AnnouncementDTO> announcements = announcementService.getActiveAnnouncements(pageable);
        return ResponseEntity.ok(ApiResponse.success(announcements));
    }

    @GetMapping("/pinned")
    public ResponseEntity<ApiResponse<List<AnnouncementDTO>>> getPinnedAnnouncements() {
        List<AnnouncementDTO> announcements = announcementService.getPinnedAnnouncements();
        return ResponseEntity.ok(ApiResponse.success(announcements));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<ApiResponse<Page<AnnouncementDTO>>> getAnnouncementsByType(
            @PathVariable String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<AnnouncementDTO> announcements = announcementService.getAnnouncementsByType(type, pageable);
        return ResponseEntity.ok(ApiResponse.success(announcements));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<Page<AnnouncementDTO>>> getAllAnnouncements(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<AnnouncementDTO> announcements = announcementService.getAllAnnouncements(pageable);
        return ResponseEntity.ok(ApiResponse.success(announcements));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStatistics() {
        Map<String, Object> stats = announcementService.getAnnouncementStatistics();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/read-statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserReadStatistics() {
        Map<String, Object> stats = announcementService.getUserReadStatistics();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
