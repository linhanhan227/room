package com.chat.room.service;

import com.chat.room.dto.*;
import com.chat.room.entity.*;
import com.chat.room.exception.BusinessException;
import com.chat.room.exception.ForbiddenException;
import com.chat.room.exception.ResourceNotFoundException;
import com.chat.room.repository.*;
import com.chat.room.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final MessageRepository messageRepository;
    private final BannedUserRepository bannedUserRepository;
    private final SystemLogRepository systemLogRepository;
    private final SensitiveWordService sensitiveWordService;

    @Transactional
    public BannedUserDTO banUser(BanUserRequest request) {
        User currentUser = getCurrentUser();
        User targetUser = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getUserId()));

        if (targetUser.getRole() == User.UserRole.ADMIN) {
            throw new ForbiddenException("Cannot ban admin user");
        }

        if (bannedUserRepository.isUserBanned(request.getUserId(), LocalDateTime.now())) {
            throw new BusinessException("User is already banned");
        }

        if (request.getType() == BannedUser.BanType.TEMPORARY && request.getEndTime() == null) {
            throw new BusinessException("End time is required for temporary ban");
        }

        BannedUser bannedUser = BannedUser.builder()
                .user(targetUser)
                .bannedBy(currentUser)
                .reason(request.getReason())
                .type(request.getType())
                .endTime(request.getEndTime())
                .active(true)
                .build();

        bannedUser = bannedUserRepository.save(bannedUser);

        targetUser.setStatus(User.UserStatus.OFFLINE);
        userRepository.save(targetUser);

        logOperation("BAN_USER", "User", targetUser.getId(), 
                "Banned user: " + targetUser.getUsername() + ", reason: " + request.getReason());

        return BannedUserDTO.fromEntity(bannedUser);
    }

    @Transactional
    public void unbanUser(Long userId) {
        User currentUser = getCurrentUser();
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        int updated = bannedUserRepository.unbanUser(userId);
        if (updated == 0) {
            throw new BusinessException("User is not banned");
        }

        logOperation("UNBAN_USER", "User", targetUser.getId(), 
                "Unbanned user: " + targetUser.getUsername());
    }

    public boolean isUserBanned(Long userId) {
        return bannedUserRepository.isUserBanned(userId, LocalDateTime.now());
    }

    public Page<BannedUserDTO> getBannedUsers(Pageable pageable) {
        return bannedUserRepository.findByActiveTrue(pageable)
                .map(BannedUserDTO::fromEntity);
    }

    public BannedUserDTO getActiveBanByUserId(Long userId) {
        BannedUser bannedUser = bannedUserRepository.findActiveBanByUserId(userId, LocalDateTime.now())
                .orElseThrow(() -> new ResourceNotFoundException("No active ban for user: " + userId));
        return BannedUserDTO.fromEntity(bannedUser);
    }

    @Transactional
    public void setUserRole(Long userId, User.UserRole role) {
        User currentUser = getCurrentUser();
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        if (currentUser.getId().equals(userId)) {
            throw new BusinessException("Cannot change your own role");
        }

        targetUser.setRole(role);
        userRepository.save(targetUser);

        logOperation("CHANGE_ROLE", "User", targetUser.getId(), 
                "Changed role to: " + role + " for user: " + targetUser.getUsername());
    }

    @Transactional
    public void deleteRoom(Long roomId) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room", roomId));

        messageRepository.deleteByRoomId(roomId);
        chatRoomRepository.delete(room);

        logOperation("DELETE_ROOM", "ChatRoom", roomId, "Deleted room: " + room.getName());
    }

    @Transactional
    public void archiveRoom(Long roomId) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room", roomId));

        room.setStatus(ChatRoom.RoomStatus.ARCHIVED);
        chatRoomRepository.save(room);

        logOperation("ARCHIVE_ROOM", "ChatRoom", roomId, "Archived room: " + room.getName());
    }

    @Transactional
    public void deleteMessage(Long messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message", messageId));

        messageRepository.delete(message);

        logOperation("DELETE_MESSAGE", "Message", messageId, "Deleted message from room: " + message.getRoom().getName());
    }

    public Page<UserDTO> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(UserDTO::fromEntity);
    }

    public Page<UserDTO> searchUsers(String keyword, Pageable pageable) {
        return userRepository.searchByKeyword(keyword, pageable)
                .map(UserDTO::fromEntity);
    }

    public Page<ChatRoomDTO> getAllRooms(Pageable pageable) {
        return chatRoomRepository.findAll(pageable)
                .map(room -> {
                    ChatRoomDTO dto = ChatRoomDTO.fromEntity(room);
                    dto.setMemberCount(chatRoomRepository.countMembersByRoomId(room.getId()));
                    return dto;
                });
    }

    public Page<SystemLogDTO> getSystemLogs(Pageable pageable) {
        return systemLogRepository.findAll(pageable)
                .map(SystemLogDTO::fromEntity);
    }

    public Page<SystemLogDTO> getSystemLogsByAction(String action, Pageable pageable) {
        return systemLogRepository.findByAction(action, pageable)
                .map(SystemLogDTO::fromEntity);
    }

    public Page<SystemLogDTO> getSystemLogsByDateRange(LocalDate start, LocalDate end, Pageable pageable) {
        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.atTime(LocalTime.MAX);
        return systemLogRepository.findByDateRange(startDateTime, endDateTime, pageable)
                .map(SystemLogDTO::fromEntity);
    }

    public Page<SystemLogDTO> getSystemLogsByUser(Long userId, Pageable pageable) {
        return systemLogRepository.findByTargetUserId(userId, pageable)
                .map(SystemLogDTO::fromEntity);
    }

    public DashboardStats getDashboardStats() {
        Long totalUsers = userRepository.count();
        Long onlineUsers = userRepository.countOnlineUsers();
        Long totalRooms = chatRoomRepository.count();
        Long activeRooms = chatRoomRepository.findAll().stream()
                .filter(r -> r.getStatus() == ChatRoom.RoomStatus.ACTIVE)
                .count();
        Long totalMessages = messageRepository.count();
        Long bannedUsers = (long) bannedUserRepository.findByActiveTrue(Pageable.unpaged()).getContent().size();
        Long sensitiveWordCount = (long) sensitiveWordService.getSensitiveWordCount();

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        Long todayMessages = messageRepository.findAll().stream()
                .filter(m -> m.getCreatedAt().isAfter(todayStart))
                .count();

        List<DashboardStats.ActiveRoomStats> topRooms = chatRoomRepository.findAll(PageRequest.of(0, 5, Sort.by("createdAt").descending()))
                .getContent().stream()
                .map(room -> DashboardStats.ActiveRoomStats.builder()
                        .roomId(room.getId())
                        .roomName(room.getName())
                        .messageCount(messageRepository.countByRoomId(room.getId()))
                        .memberCount(chatRoomRepository.countMembersByRoomId(room.getId()))
                        .build())
                .collect(Collectors.toList());

        return DashboardStats.builder()
                .totalUsers(totalUsers)
                .onlineUsers(onlineUsers)
                .totalRooms(totalRooms)
                .activeRooms(activeRooms)
                .totalMessages(totalMessages)
                .todayMessages(todayMessages)
                .bannedUsers(bannedUsers)
                .sensitiveWordCount(sensitiveWordCount)
                .topActiveRooms(topRooms)
                .build();
    }

    public List<String> getLogActions() {
        return systemLogRepository.findAllActions();
    }

    private void logOperation(String action, String entityType, Long entityId, String details) {
        User currentUser = getCurrentUser();
        SystemLog log = SystemLog.builder()
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .operator(currentUser)
                .targetUserId(entityType.equals("User") ? entityId : null)
                .details(details)
                .level(SystemLog.LogLevel.INFO)
                .build();
        systemLogRepository.save(log);
    }

    private User getCurrentUser() {
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", principal.getId()));
    }
}
