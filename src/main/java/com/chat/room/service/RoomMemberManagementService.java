package com.chat.room.service;

import com.chat.room.dto.BlacklistMemberDTO;
import com.chat.room.dto.BlacklistMemberRequest;
import com.chat.room.dto.MuteMemberRequest;
import com.chat.room.dto.UserDTO;
import com.chat.room.entity.ChatRoom;
import com.chat.room.entity.RoomBlacklist;
import com.chat.room.entity.RoomMember;
import com.chat.room.entity.User;
import com.chat.room.exception.BusinessException;
import com.chat.room.exception.ForbiddenException;
import com.chat.room.exception.ResourceNotFoundException;
import com.chat.room.repository.ChatRoomRepository;
import com.chat.room.repository.RoomBlacklistRepository;
import com.chat.room.repository.RoomMemberRepository;
import com.chat.room.repository.UserRepository;
import com.chat.room.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomMemberManagementService {

    private final RoomMemberRepository roomMemberRepository;
    private final RoomBlacklistRepository roomBlacklistRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", principal.getId()));
    }

    private boolean isSystemAdmin(User user) {
        return user.getRole() == User.UserRole.ADMIN;
    }

    private RoomMember getRoomMember(Long roomId, Long userId) {
        return roomMemberRepository.findByRoomIdAndUserId(roomId, userId)
                .orElseThrow(() -> new BusinessException("用户不是该聊天室的成员"));
    }

    private RoomMember getAndValidateRoomMember(Long roomId, Long userId, String action) {
        RoomMember member = getRoomMember(roomId, userId);
        if (member.getRoom().getOwner().getId().equals(userId)) {
            throw new BusinessException("不能对聊天室所有者执行" + action + "操作");
        }
        return member;
    }

    private void validatePermission(User currentUser, ChatRoom room, RoomMember.MemberRole requiredRole) {
        if (isSystemAdmin(currentUser)) {
            return;
        }

        RoomMember.MemberRole currentUserRole = roomMemberRepository
                .findRoleByRoomIdAndUserId(room.getId(), currentUser.getId())
                .orElseThrow(() -> new BusinessException("您不是该聊天室的成员"));

        if (currentUserRole.ordinal() > requiredRole.ordinal()) {
            throw new ForbiddenException("您没有权限执行此操作");
        }
    }

    @Transactional
    public void muteMember(Long roomId, MuteMemberRequest request) {
        User currentUser = getCurrentUser();
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room", roomId));

        validatePermission(currentUser, room, RoomMember.MemberRole.ADMIN);

        RoomMember targetMember = getAndValidateRoomMember(roomId, request.getUserId(), "禁言");

        if (isSystemAdmin(userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getUserId())))) {
            throw new ForbiddenException("不能对系统管理员执行禁言操作");
        }

        targetMember.setMuted(true);
        if (request.getDurationMinutes() != null && request.getDurationMinutes() > 0) {
            targetMember.setMutedUntil(LocalDateTime.now().plusMinutes(request.getDurationMinutes()));
        }
        roomMemberRepository.save(targetMember);

        log.info("User {} muted user {} in room {} for reason: {}", 
                currentUser.getId(), request.getUserId(), roomId, request.getReason());
    }

    @Transactional
    public void unmuteMember(Long roomId, Long userId) {
        User currentUser = getCurrentUser();
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room", roomId));

        validatePermission(currentUser, room, RoomMember.MemberRole.ADMIN);

        RoomMember targetMember = getRoomMember(roomId, userId);
        targetMember.setMuted(false);
        targetMember.setMutedUntil(null);
        roomMemberRepository.save(targetMember);

        log.info("User {} unmuted user {} in room {}", currentUser.getId(), userId, roomId);
    }

    @Transactional
    public void kickMember(Long roomId, Long userId) {
        User currentUser = getCurrentUser();
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room", roomId));

        validatePermission(currentUser, room, RoomMember.MemberRole.ADMIN);

        RoomMember targetMember = getAndValidateRoomMember(roomId, userId, "踢出");

        if (isSystemAdmin(userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId)))) {
            throw new ForbiddenException("不能踢出系统管理员");
        }

        roomMemberRepository.deleteByRoomIdAndUserId(roomId, userId);

        log.info("User {} kicked user {} from room {}", currentUser.getId(), userId, roomId);
    }

    @Transactional
    public void blacklistMember(Long roomId, BlacklistMemberRequest request) {
        User currentUser = getCurrentUser();
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room", roomId));

        validatePermission(currentUser, room, RoomMember.MemberRole.ADMIN);

        User targetUser = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getUserId()));

        if (isSystemAdmin(targetUser)) {
            throw new ForbiddenException("不能拉黑系统管理员");
        }

        if (targetUser.getId().equals(room.getOwner().getId())) {
            throw new BusinessException("不能拉黑聊天室所有者");
        }

        if (roomBlacklistRepository.existsByRoomIdAndUserId(roomId, request.getUserId())) {
            throw new BusinessException("该用户已在黑名单中");
        }

        RoomBlacklist blacklist = RoomBlacklist.builder()
                .room(room)
                .user(targetUser)
                .addedBy(currentUser)
                .reason(request.getReason())
                .build();
        roomBlacklistRepository.save(blacklist);

        roomMemberRepository.deleteByRoomIdAndUserId(roomId, request.getUserId());

        log.info("User {} blacklisted user {} in room {} for reason: {}", 
                currentUser.getId(), request.getUserId(), roomId, request.getReason());
    }

    @Transactional
    public void removeFromBlacklist(Long roomId, Long userId) {
        User currentUser = getCurrentUser();
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room", roomId));

        validatePermission(currentUser, room, RoomMember.MemberRole.ADMIN);

        if (!roomBlacklistRepository.existsByRoomIdAndUserId(roomId, userId)) {
            throw new BusinessException("该用户不在黑名单中");
        }

        roomBlacklistRepository.deleteByRoomIdAndUserId(roomId, userId);

        log.info("User {} removed user {} from blacklist in room {}", currentUser.getId(), userId, roomId);
    }

    public List<BlacklistMemberDTO> getBlacklist(Long roomId) {
        User currentUser = getCurrentUser();
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room", roomId));

        validatePermission(currentUser, room, RoomMember.MemberRole.ADMIN);

        return roomBlacklistRepository.findByRoomId(roomId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private BlacklistMemberDTO convertToDTO(RoomBlacklist blacklist) {
        return BlacklistMemberDTO.builder()
                .id(blacklist.getId())
                .roomId(blacklist.getRoom().getId())
                .roomName(blacklist.getRoom().getName())
                .userId(blacklist.getUser().getId())
                .username(blacklist.getUser().getUsername())
                .nickname(blacklist.getUser().getNickname())
                .addedById(blacklist.getAddedBy().getId())
                .addedByName(blacklist.getAddedBy().getNickname() != null ? 
                        blacklist.getAddedBy().getNickname() : blacklist.getAddedBy().getUsername())
                .reason(blacklist.getReason())
                .createdAt(blacklist.getCreatedAt())
                .build();
    }

    public boolean isUserBlacklisted(Long roomId, Long userId) {
        return roomBlacklistRepository.existsByRoomIdAndUserId(roomId, userId);
    }

    @Transactional
    public boolean isUserMuted(Long roomId, Long userId) {
        RoomMember member = roomMemberRepository.findByRoomIdAndUserId(roomId, userId).orElse(null);
        if (member == null || !member.getMuted()) {
            return false;
        }
        if (member.getMutedUntil() != null && member.getMutedUntil().isBefore(LocalDateTime.now())) {
            member.setMuted(false);
            member.setMutedUntil(null);
            roomMemberRepository.save(member);
            return false;
        }
        return true;
    }

    public List<UserDTO> getRoomMembers(Long roomId) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room", roomId));
        return userRepository.findByRoomId(roomId).stream()
                .map(user -> {
                    UserDTO dto = UserDTO.fromEntity(user);
                    RoomMember member = roomMemberRepository.findByRoomIdAndUserId(roomId, user.getId()).orElse(null);
                    if (member != null) {
                        dto.setRoomRole(member.getRole());
                        dto.setMuted(member.getMuted());
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void setMemberRole(Long roomId, Long userId, RoomMember.MemberRole role) {
        User currentUser = getCurrentUser();
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room", roomId));

        if (!isSystemAdmin(currentUser) && !room.getOwner().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("只有聊天室所有者或系统管理员可以设置成员角色");
        }

        RoomMember targetMember = getRoomMember(roomId, userId);

        if (targetMember.getRole() == RoomMember.MemberRole.OWNER) {
            throw new BusinessException("不能修改聊天室所有者的角色");
        }

        targetMember.setRole(role);
        roomMemberRepository.save(targetMember);

        log.info("User {} set role {} for user {} in room {}", currentUser.getId(), role, userId, roomId);
    }
}
