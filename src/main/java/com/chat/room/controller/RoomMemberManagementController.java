package com.chat.room.controller;

import com.chat.room.dto.ApiResponse;
import com.chat.room.dto.BlacklistMemberDTO;
import com.chat.room.dto.BlacklistMemberRequest;
import com.chat.room.dto.MuteMemberRequest;
import com.chat.room.dto.UserDTO;
import com.chat.room.service.RoomMemberManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rooms/{roomId}/members")
@RequiredArgsConstructor
public class RoomMemberManagementController {

    private final RoomMemberManagementService roomMemberManagementService;

    @PostMapping("/mute")
    public ResponseEntity<ApiResponse<Void>> muteMember(
            @PathVariable Long roomId,
            @Valid @RequestBody MuteMemberRequest request) {
        roomMemberManagementService.muteMember(roomId, request);
        return ResponseEntity.ok(ApiResponse.success("成员禁言成功", null));
    }

    @PostMapping("/{userId}/unmute")
    public ResponseEntity<ApiResponse<Void>> unmuteMember(
            @PathVariable Long roomId,
            @PathVariable Long userId) {
        roomMemberManagementService.unmuteMember(roomId, userId);
        return ResponseEntity.ok(ApiResponse.success("成员解禁成功", null));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<Void>> kickMember(
            @PathVariable Long roomId,
            @PathVariable Long userId) {
        roomMemberManagementService.kickMember(roomId, userId);
        return ResponseEntity.ok(ApiResponse.success("成员踢出成功", null));
    }

    @PostMapping("/blacklist")
    public ResponseEntity<ApiResponse<Void>> blacklistMember(
            @PathVariable Long roomId,
            @Valid @RequestBody BlacklistMemberRequest request) {
        roomMemberManagementService.blacklistMember(roomId, request);
        return ResponseEntity.ok(ApiResponse.success("成员拉黑成功", null));
    }

    @DeleteMapping("/blacklist/{userId}")
    public ResponseEntity<ApiResponse<Void>> removeFromBlacklist(
            @PathVariable Long roomId,
            @PathVariable Long userId) {
        roomMemberManagementService.removeFromBlacklist(roomId, userId);
        return ResponseEntity.ok(ApiResponse.success("成员解除拉黑成功", null));
    }

    @GetMapping("/blacklist")
    public ResponseEntity<ApiResponse<List<BlacklistMemberDTO>>> getBlacklist(
            @PathVariable Long roomId) {
        List<BlacklistMemberDTO> blacklist = roomMemberManagementService.getBlacklist(roomId);
        return ResponseEntity.ok(ApiResponse.success(blacklist));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserDTO>>> getRoomMembers(
            @PathVariable Long roomId) {
        List<UserDTO> members = roomMemberManagementService.getRoomMembers(roomId);
        return ResponseEntity.ok(ApiResponse.success(members));
    }
}
