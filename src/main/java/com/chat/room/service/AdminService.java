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
    private final RoomMemberRepository roomMemberRepository;
    private final RoomBlacklistRepository roomBlacklistRepository;
    private final SystemLogRepository systemLogRepository;

    @Transactional
    public BannedUserDTO banUser(BanUserRequest request) {
        User currentUser = getCurrentUser();
        User targetUser = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("用户", request.getUserId()));

        if (targetUser.getRole() == User.UserRole.ADMIN) {
            throw new ForbiddenException("不能封禁管理员用户");
        }

        if (bannedUserRepository.isUserBanned(request.getUserId(), LocalDateTime.now())) {
            throw new BusinessException("用户已被封禁");
        }

        if (request.getType() == BannedUser.BanType.TEMPORARY && request.getEndTime() == null) {
            throw new BusinessException("临时封禁需要指定结束时间");
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
                .orElseThrow(() -> new ResourceNotFoundException("用户", userId));

        int updated = bannedUserRepository.unbanUser(userId);
        if (updated == 0) {
            throw new BusinessException("用户未被封禁");
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
                .orElseThrow(() -> new ResourceNotFoundException("用户", userId));

        if (currentUser.getId().equals(userId)) {
            throw new BusinessException("不能修改自己的角色");
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
        roomBlacklistRepository.deleteByRoomId(roomId);
        roomMemberRepository.deleteByRoomId(roomId);
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
        Long activeRooms = chatRoomRepository.findPublicRooms(Pageable.unpaged()).getTotalElements();
        Long totalMessages = messageRepository.count();
        Long bannedUsers = bannedUserRepository.countActiveBans();

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        Long todayMessages = messageRepository.countMessagesSince(todayStart);

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
