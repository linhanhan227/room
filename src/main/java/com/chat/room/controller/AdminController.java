package com.chat.room.controller;

import com.chat.room.dto.*;
import com.chat.room.entity.User;
import com.chat.room.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardStats>> getDashboardStats() {
        DashboardStats stats = adminService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<Page<UserDTO>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<UserDTO> users = adminService.getAllUsers(pageable);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping("/users/search")
    public ResponseEntity<ApiResponse<Page<UserDTO>>> searchUsers(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<UserDTO> users = adminService.searchUsers(keyword, pageable);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @PutMapping("/users/{userId}/role")
    public ResponseEntity<ApiResponse<Void>> setUserRole(
            @PathVariable Long userId,
            @RequestParam User.UserRole role) {
        adminService.setUserRole(userId, role);
        return ResponseEntity.ok(ApiResponse.success("User role updated successfully", null));
    }

    @PostMapping("/users/ban")
    public ResponseEntity<ApiResponse<BannedUserDTO>> banUser(@Valid @RequestBody BanUserRequest request) {
        BannedUserDTO bannedUser = adminService.banUser(request);
        return ResponseEntity.ok(ApiResponse.success("User banned successfully", bannedUser));
    }

    @DeleteMapping("/users/{userId}/ban")
    public ResponseEntity<ApiResponse<Void>> unbanUser(@PathVariable Long userId) {
        adminService.unbanUser(userId);
        return ResponseEntity.ok(ApiResponse.success("User unbanned successfully", null));
    }

    @GetMapping("/users/banned")
    public ResponseEntity<ApiResponse<Page<BannedUserDTO>>> getBannedUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<BannedUserDTO> bannedUsers = adminService.getBannedUsers(pageable);
        return ResponseEntity.ok(ApiResponse.success(bannedUsers));
    }

    @GetMapping("/users/{userId}/ban")
    public ResponseEntity<ApiResponse<BannedUserDTO>> getUserBanStatus(@PathVariable Long userId) {
        BannedUserDTO bannedUser = adminService.getActiveBanByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(bannedUser));
    }

    @GetMapping("/rooms")
    public ResponseEntity<ApiResponse<Page<ChatRoomDTO>>> getAllRooms(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ChatRoomDTO> rooms = adminService.getAllRooms(pageable);
        return ResponseEntity.ok(ApiResponse.success(rooms));
    }

    @DeleteMapping("/rooms/{roomId}")
    public ResponseEntity<ApiResponse<Void>> deleteRoom(@PathVariable Long roomId) {
        adminService.deleteRoom(roomId);
        return ResponseEntity.ok(ApiResponse.success("Room deleted successfully", null));
    }

    @PutMapping("/rooms/{roomId}/archive")
    public ResponseEntity<ApiResponse<Void>> archiveRoom(@PathVariable Long roomId) {
        adminService.archiveRoom(roomId);
        return ResponseEntity.ok(ApiResponse.success("Room archived successfully", null));
    }

    @DeleteMapping("/messages/{messageId}")
    public ResponseEntity<ApiResponse<Void>> deleteMessage(@PathVariable Long messageId) {
        adminService.deleteMessage(messageId);
        return ResponseEntity.ok(ApiResponse.success("Message deleted successfully", null));
    }

    @GetMapping("/logs")
    public ResponseEntity<ApiResponse<Page<SystemLogDTO>>> getSystemLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<SystemLogDTO> logs = adminService.getSystemLogs(pageable);
        return ResponseEntity.ok(ApiResponse.success(logs));
    }

    @GetMapping("/logs/actions")
    public ResponseEntity<ApiResponse<List<String>>> getLogActions() {
        List<String> actions = adminService.getLogActions();
        return ResponseEntity.ok(ApiResponse.success(actions));
    }

    @GetMapping("/logs/by-action")
    public ResponseEntity<ApiResponse<Page<SystemLogDTO>>> getLogsByAction(
            @RequestParam String action,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<SystemLogDTO> logs = adminService.getSystemLogsByAction(action, pageable);
        return ResponseEntity.ok(ApiResponse.success(logs));
    }

    @GetMapping("/logs/by-date")
    public ResponseEntity<ApiResponse<Page<SystemLogDTO>>> getLogsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<SystemLogDTO> logs = adminService.getSystemLogsByDateRange(start, end, pageable);
        return ResponseEntity.ok(ApiResponse.success(logs));
    }

    @GetMapping("/logs/by-user/{userId}")
    public ResponseEntity<ApiResponse<Page<SystemLogDTO>>> getLogsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<SystemLogDTO> logs = adminService.getSystemLogsByUser(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(logs));
    }
}
