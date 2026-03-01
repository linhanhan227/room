package com.chat.room.controller;

import com.chat.room.dto.*;
import com.chat.room.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping
    public ResponseEntity<ApiResponse<MessageDTO>> sendMessage(@Valid @RequestBody SendMessageRequest request) {
        MessageDTO message = messageService.sendMessage(request);
        return ResponseEntity.ok(ApiResponse.success("Message sent successfully", message));
    }

    @GetMapping("/room/{roomId}")
    public ResponseEntity<ApiResponse<Page<MessageDTO>>> getRoomMessages(
            @PathVariable Long roomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<MessageDTO> messages = messageService.getRoomMessages(roomId,
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ResponseEntity.ok(ApiResponse.success(messages));
    }

    @GetMapping("/room/{roomId}/recent")
    public ResponseEntity<ApiResponse<List<MessageDTO>>> getRecentMessages(
            @PathVariable Long roomId,
            @RequestParam(defaultValue = "50") int limit) {
        List<MessageDTO> messages = messageService.getRecentMessages(roomId, limit);
        return ResponseEntity.ok(ApiResponse.success(messages));
    }

    @GetMapping("/room/{roomId}/search")
    public ResponseEntity<ApiResponse<Page<MessageDTO>>> searchMessages(
            @PathVariable Long roomId,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<MessageDTO> messages = messageService.searchMessages(roomId, keyword,
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ResponseEntity.ok(ApiResponse.success(messages));
    }

    @DeleteMapping("/{messageId}")
    public ResponseEntity<ApiResponse<Void>> deleteMessage(@PathVariable Long messageId) {
        messageService.deleteMessage(messageId);
        return ResponseEntity.ok(ApiResponse.success("Message deleted successfully", null));
    }

    @GetMapping("/room/{roomId}/count")
    public ResponseEntity<ApiResponse<Long>> getMessageCount(@PathVariable Long roomId) {
        Long count = messageService.getMessageCount(roomId);
        return ResponseEntity.ok(ApiResponse.success(count));
    }
}
