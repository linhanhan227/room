package com.chat.room.service;

import com.chat.room.dto.MessageDTO;
import com.chat.room.dto.SendMessageRequest;
import com.chat.room.entity.ChatRoom;
import com.chat.room.entity.Message;
import com.chat.room.entity.User;
import com.chat.room.exception.BusinessException;
import com.chat.room.exception.ResourceNotFoundException;
import com.chat.room.repository.BannedUserRepository;
import com.chat.room.repository.ChatRoomRepository;
import com.chat.room.repository.MessageRepository;
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
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final BannedUserRepository bannedUserRepository;
    private final SensitiveWordService sensitiveWordService;

    @Transactional
    public MessageDTO sendMessage(SendMessageRequest request) {
        User sender = getCurrentUser();

        if (bannedUserRepository.isUserBanned(sender.getId(), LocalDateTime.now())) {
            throw new BusinessException("You are banned from sending messages");
        }

        ChatRoom room = chatRoomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room", request.getRoomId()));

        if (!chatRoomRepository.isUserInRoom(room.getId(), sender.getId())) {
            throw new BusinessException("You are not a member of this room");
        }

        String content = request.getContent();
        if (sensitiveWordService.containsSensitiveWord(content)) {
            content = sensitiveWordService.filterText(content);
            log.info("Message from user {} contained sensitive words, filtered", sender.getId());
        }

        Message message = Message.builder()
                .room(room)
                .sender(sender)
                .content(content)
                .type(request.getType() != null ? Message.MessageType.valueOf(request.getType().toUpperCase()) : Message.MessageType.TEXT)
                .build();

        message = messageRepository.save(message);
        return MessageDTO.fromEntity(message);
    }

    @Transactional
    public MessageDTO saveMessage(Long roomId, Long senderId, String content, Message.MessageType type) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room", roomId));
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("User", senderId));

        if (sensitiveWordService.containsSensitiveWord(content)) {
            content = sensitiveWordService.filterText(content);
        }

        Message message = Message.builder()
                .room(room)
                .sender(sender)
                .content(content)
                .type(type)
                .build();

        message = messageRepository.save(message);
        return MessageDTO.fromEntity(message);
    }

    @Transactional
    public MessageDTO sendMessageWithFilter(Long roomId, Long senderId, String content, Message.MessageType type) {
        if (bannedUserRepository.isUserBanned(senderId, LocalDateTime.now())) {
            throw new BusinessException("User is banned from sending messages");
        }

        return saveMessage(roomId, senderId, content, type);
    }

    public Page<MessageDTO> getRoomMessages(Long roomId, Pageable pageable) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room", roomId));

        return messageRepository.findByRoomIdOrderByCreatedAtDesc(roomId, pageable)
                .map(MessageDTO::fromEntity);
    }

    public List<MessageDTO> getRecentMessages(Long roomId, int limit) {
        return messageRepository.findRecentMessages(roomId, limit).stream()
                .map(MessageDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<MessageDTO> getMessagesAfter(Long roomId, LocalDateTime after) {
        return messageRepository.findByRoomIdAndCreatedAtAfterOrderByCreatedAtAsc(roomId, after).stream()
                .map(MessageDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public Page<MessageDTO> searchMessages(Long roomId, String keyword, Pageable pageable) {
        return messageRepository.searchByContent(roomId, keyword, pageable)
                .map(MessageDTO::fromEntity);
    }

    @Transactional
    public void deleteMessage(Long messageId) {
        User user = getCurrentUser();
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message", messageId));

        if (!message.getSender().getId().equals(user.getId())) {
            throw new BusinessException("You can only delete your own messages");
        }

        messageRepository.delete(message);
    }

    public Long getMessageCount(Long roomId) {
        return messageRepository.countByRoomId(roomId);
    }

    public Long getUserMessageCount(Long roomId, Long userId) {
        return messageRepository.countByRoomIdAndSenderId(roomId, userId);
    }

    private User getCurrentUser() {
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", principal.getId()));
    }
}
