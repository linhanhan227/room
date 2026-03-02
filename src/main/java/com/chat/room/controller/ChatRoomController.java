package com.chat.room.controller;

import com.chat.room.dto.*;
import com.chat.room.entity.RoomMember;
import com.chat.room.entity.User;
import com.chat.room.repository.UserRepository;
import com.chat.room.security.UserPrincipal;
import com.chat.room.service.ChatRoomService;
import com.chat.room.service.recommendation.RoomRecommendationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/rooms")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;
    private final RoomRecommendationService recommendationService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<ApiResponse<ChatRoomDTO>> createRoom(@Valid @RequestBody CreateRoomRequest request) {
        ChatRoomDTO room = chatRoomService.createRoom(request);
        return ResponseEntity.ok(ApiResponse.success("聊天室创建成功", room));
    }

    @PostMapping("/{roomId}/join")
    public ResponseEntity<ApiResponse<ChatRoomDTO>> joinRoom(
            @PathVariable Long roomId,
            @RequestParam(required = false) String password) {
        ChatRoomDTO room = chatRoomService.joinRoom(roomId, password);
        return ResponseEntity.ok(ApiResponse.success("加入聊天室成功", room));
    }

    @PostMapping("/{roomId}/leave")
    public ResponseEntity<ApiResponse<Void>> leaveRoom(@PathVariable Long roomId) {
        chatRoomService.leaveRoom(roomId);
        return ResponseEntity.ok(ApiResponse.success("退出聊天室成功", null));
    }

    @DeleteMapping("/{roomId}")
    public ResponseEntity<ApiResponse<Void>> deleteRoom(@PathVariable Long roomId) {
        chatRoomService.deleteRoom(roomId);
        return ResponseEntity.ok(ApiResponse.success("聊天室删除成功", null));
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<ApiResponse<ChatRoomDTO>> getRoomById(@PathVariable Long roomId) {
        ChatRoomDTO room = chatRoomService.getRoomById(roomId);
        return ResponseEntity.ok(ApiResponse.success(room));
    }

    @GetMapping("/public")
    public ResponseEntity<ApiResponse<Page<ChatRoomDTO>>> getPublicRooms(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<ChatRoomDTO> rooms = chatRoomService.getPublicRooms(
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ResponseEntity.ok(ApiResponse.success(rooms));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<ChatRoomDTO>>> getMyRooms() {
        List<ChatRoomDTO> rooms = chatRoomService.getMyRooms();
        return ResponseEntity.ok(ApiResponse.success(rooms));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<ChatRoomDTO>>> searchRooms(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<ChatRoomDTO> rooms = chatRoomService.searchRooms(keyword,
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ResponseEntity.ok(ApiResponse.success(rooms));
    }

    @PutMapping("/{roomId}")
    public ResponseEntity<ApiResponse<ChatRoomDTO>> updateRoom(
            @PathVariable Long roomId,
            @Valid @RequestBody CreateRoomRequest request) {
        ChatRoomDTO room = chatRoomService.updateRoom(roomId, request);
        return ResponseEntity.ok(ApiResponse.success("聊天室更新成功", room));
    }

    @GetMapping("/recommendations")
    public ResponseEntity<ApiResponse<List<ChatRoomDTO>>> getRecommendations(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) String strategy,
            @RequestParam(defaultValue = "10") int limit) {
        
        List<ChatRoomDTO> recommendations;
        
        if (principal != null) {
            User user = userRepository.findById(principal.getId()).orElse(null);
            if (strategy != null && !strategy.isEmpty()) {
                recommendations = recommendationService.getRecommendations(strategy, user, limit);
            } else {
                recommendations = recommendationService.getRecommendations(user, limit);
            }
        } else {
            if (strategy != null && !strategy.isEmpty()) {
                recommendations = recommendationService.getRecommendations(strategy, null, limit);
            } else {
                recommendations = recommendationService.getRecommendationsForAnonymous(limit);
            }
        }
        
        return ResponseEntity.ok(ApiResponse.success(recommendations));
    }

    @GetMapping("/recommendations/strategies")
    public ResponseEntity<ApiResponse<List<String>>> getAvailableStrategies() {
        List<String> strategies = recommendationService.getAvailableStrategies();
        return ResponseEntity.ok(ApiResponse.success(strategies));
    }

    @GetMapping("/recommendations/default")
    public ResponseEntity<ApiResponse<String>> getDefaultStrategy() {
        String defaultStrategy = recommendationService.getDefaultStrategyName();
        return ResponseEntity.ok(ApiResponse.success(defaultStrategy));
    }
}
