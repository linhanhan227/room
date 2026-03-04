package com.chat.room.controller;

import com.chat.room.dto.*;
import com.chat.room.entity.User;
import com.chat.room.exception.ForbiddenException;
import com.chat.room.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDTO>> getCurrentUser() {
        UserDTO user = userService.getCurrentUser();
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable Long id) {
        UserDTO user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<ApiResponse<UserDTO>> getUserByUsername(@PathVariable String username) {
        UserDTO user = userService.getUserByUsername(username);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<UserDTO>>> searchUsers(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<UserDTO> users = userService.searchUsers(keyword, 
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping("/online")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getOnlineUsers() {
        List<UserDTO> users = userService.getOnlineUsers();
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping("/online/count")
    public ResponseEntity<ApiResponse<Long>> getOnlineUserCount() {
        Long count = userService.getOnlineUserCount();
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserDTO>> updateProfile(
            @RequestParam(required = false) String nickname,
            @RequestParam(required = false) String avatar,
            @RequestParam(required = false) String email) {
        UserDTO user = userService.updateProfile(userService.getCurrentUserId(), nickname, avatar, email);
        return ResponseEntity.ok(ApiResponse.success("个人资料更新成功", user));
    }

    @PutMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(userService.getCurrentUserId(), request.getOldPassword(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success("密码修改成功", null));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<UserDTO>> updateUserStatus(
            @PathVariable Long id,
            @RequestParam User.UserStatus status) {
        Long currentUserId = userService.getCurrentUserId();
        if (!currentUserId.equals(id)) {
            throw new ForbiddenException("您没有权限修改其他用户的状态");
        }
        UserDTO user = userService.updateUserStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("状态更新成功", user));
    }
}
