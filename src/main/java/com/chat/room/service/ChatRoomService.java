package com.chat.room.service;

import com.chat.room.dto.ChatRoomDTO;
import com.chat.room.dto.CreateRoomRequest;
import com.chat.room.entity.ChatRoom;
import com.chat.room.entity.RoomMember;
import com.chat.room.entity.User;
import com.chat.room.exception.BusinessException;
import com.chat.room.exception.ForbiddenException;
import com.chat.room.exception.ResourceNotFoundException;
import com.chat.room.repository.ChatRoomRepository;
import com.chat.room.repository.MessageRepository;
import com.chat.room.repository.RoomMemberRepository;
import com.chat.room.repository.UserRepository;
import com.chat.room.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final RoomMemberRepository roomMemberRepository;
    private final MessageRepository messageRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public ChatRoomDTO createRoom(CreateRoomRequest request) {
        User owner = getCurrentUser();

        if (chatRoomRepository.existsByName(request.getName())) {
            throw new BusinessException("聊天室名称已被使用");
        }

        ChatRoom room = ChatRoom.builder()
                .name(request.getName())
                .description(request.getDescription())
                .avatar(request.getAvatar())
                .owner(owner)
                .type(request.getType() != null ? ChatRoom.RoomType.valueOf(request.getType().toUpperCase()) : ChatRoom.RoomType.PUBLIC)
                .password(request.getPassword() != null ? passwordEncoder.encode(request.getPassword()) : null)
                .maxMembers(request.getMaxMembers() != null ? request.getMaxMembers() : 100)
                .status(ChatRoom.RoomStatus.ACTIVE)
                .build();

        room = chatRoomRepository.save(room);

        RoomMember roomMember = RoomMember.builder()
                .room(room)
                .user(owner)
                .role(RoomMember.MemberRole.OWNER)
                .build();
        roomMemberRepository.save(roomMember);

        ChatRoomDTO dto = ChatRoomDTO.fromEntity(room);
        dto.setMemberCount(1);
        return dto;
    }

    @Transactional
    public ChatRoomDTO joinRoom(Long roomId, String password) {
        User user = getCurrentUser();
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room", roomId));

        if (room.getStatus() != ChatRoom.RoomStatus.ACTIVE) {
            throw new BusinessException("聊天室未激活");
        }

        if (room.getType() == ChatRoom.RoomType.PRIVATE) {
            if (password == null || room.getPassword() == null || !passwordEncoder.matches(password, room.getPassword())) {
                throw new BusinessException("聊天室密码错误");
            }
        }

        if (roomMemberRepository.existsByRoomIdAndUserId(roomId, user.getId())) {
            throw new BusinessException("您已经是该聊天室的成员");
        }

        int memberCount = chatRoomRepository.countMembersByRoomId(roomId);
        if (memberCount >= room.getMaxMembers()) {
            throw new BusinessException("聊天室已满员");
        }

        RoomMember roomMember = RoomMember.builder()
                .room(room)
                .user(user)
                .role(RoomMember.MemberRole.MEMBER)
                .build();
        roomMemberRepository.save(roomMember);

        ChatRoomDTO dto = ChatRoomDTO.fromEntity(room);
        dto.setMemberCount(memberCount + 1);
        return dto;
    }

    @Transactional
    public void leaveRoom(Long roomId) {
        User user = getCurrentUser();
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room", roomId));

        if (!roomMemberRepository.existsByRoomIdAndUserId(roomId, user.getId())) {
            throw new BusinessException("您不是该聊天室的成员");
        }

        if (room.getOwner().getId().equals(user.getId())) {
            throw new BusinessException("聊天室所有者不能退出，请转让所有权或删除聊天室");
        }

        roomMemberRepository.deleteByRoomIdAndUserId(roomId, user.getId());
    }

    @Transactional
    public void deleteRoom(Long roomId) {
        User user = getCurrentUser();
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room", roomId));

        if (!room.getOwner().getId().equals(user.getId()) && user.getRole() != User.UserRole.ADMIN) {
            throw new ForbiddenException("只有聊天室所有者或系统管理员可以删除聊天室");
        }

        messageRepository.deleteByRoomId(roomId);
        roomMemberRepository.deleteByRoomId(roomId);
        chatRoomRepository.delete(room);
    }

    public ChatRoomDTO getRoomById(Long roomId) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room", roomId));

        ChatRoomDTO dto = ChatRoomDTO.fromEntity(room);
        dto.setMemberCount(chatRoomRepository.countMembersByRoomId(roomId));
        return dto;
    }

    public Page<ChatRoomDTO> getPublicRooms(Pageable pageable) {
        Page<ChatRoom> rooms = chatRoomRepository.findPublicRooms(pageable);
        List<ChatRoomDTO> dtos = rooms.getContent().stream()
                .map(room -> {
                    ChatRoomDTO dto = ChatRoomDTO.fromEntity(room);
                    dto.setMemberCount(chatRoomRepository.countMembersByRoomId(room.getId()));
                    return dto;
                })
                .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, rooms.getTotalElements());
    }

    public List<ChatRoomDTO> getMyRooms() {
        User user = getCurrentUser();
        List<ChatRoom> rooms = chatRoomRepository.findByMemberId(user.getId());
        return rooms.stream()
                .map(room -> {
                    ChatRoomDTO dto = ChatRoomDTO.fromEntity(room);
                    dto.setMemberCount(chatRoomRepository.countMembersByRoomId(room.getId()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public Page<ChatRoomDTO> searchRooms(String keyword, Pageable pageable) {
        Page<ChatRoom> rooms = chatRoomRepository.searchPublicRooms(keyword, pageable);
        List<ChatRoomDTO> dtos = rooms.getContent().stream()
                .map(room -> {
                    ChatRoomDTO dto = ChatRoomDTO.fromEntity(room);
                    dto.setMemberCount(chatRoomRepository.countMembersByRoomId(room.getId()));
                    return dto;
                })
                .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, rooms.getTotalElements());
    }

    public List<User> getRoomMembers(Long roomId) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room", roomId));
        return userRepository.findByRoomId(roomId);
    }

    @Transactional
    public void kickMember(Long roomId, Long userId) {
        User currentUser = getCurrentUser();
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room", roomId));

        if (currentUser.getRole() != User.UserRole.ADMIN) {
            RoomMember.MemberRole currentRole = roomMemberRepository
                    .findRoleByRoomIdAndUserId(roomId, currentUser.getId())
                    .orElseThrow(() -> new BusinessException("You are not a member of this room"));

            if (currentRole != RoomMember.MemberRole.OWNER && currentRole != RoomMember.MemberRole.ADMIN) {
                throw new ForbiddenException("您没有权限踢出成员");
            }
        }

        if (room.getOwner().getId().equals(userId)) {
            throw new BusinessException("不能踢出聊天室所有者");
        }

        roomMemberRepository.deleteByRoomIdAndUserId(roomId, userId);
    }

    @Transactional
    public void setMemberRole(Long roomId, Long userId, RoomMember.MemberRole role) {
        User currentUser = getCurrentUser();
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room", roomId));

        if (!room.getOwner().getId().equals(currentUser.getId()) && currentUser.getRole() != User.UserRole.ADMIN) {
            throw new ForbiddenException("只有聊天室所有者或系统管理员可以修改成员角色");
        }

        RoomMember roomMember = roomMemberRepository.findByRoomIdAndUserId(roomId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found in room"));

        roomMember.setRole(role);
        roomMemberRepository.save(roomMember);
    }

    @Transactional
    public ChatRoomDTO updateRoom(Long roomId, CreateRoomRequest request) {
        User user = getCurrentUser();
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room", roomId));

        if (!room.getOwner().getId().equals(user.getId()) && user.getRole() != User.UserRole.ADMIN) {
            throw new ForbiddenException("只有聊天室所有者或系统管理员可以更新聊天室");
        }

        if (request.getName() != null && !request.getName().equals(room.getName())) {
            if (chatRoomRepository.existsByName(request.getName())) {
                throw new BusinessException("聊天室名称已被使用");
            }
            room.setName(request.getName());
        }
        if (request.getDescription() != null) {
            room.setDescription(request.getDescription());
        }
        if (request.getAvatar() != null) {
            room.setAvatar(request.getAvatar());
        }
        if (request.getMaxMembers() != null) {
            room.setMaxMembers(request.getMaxMembers());
        }

        room = chatRoomRepository.save(room);
        ChatRoomDTO dto = ChatRoomDTO.fromEntity(room);
        dto.setMemberCount(chatRoomRepository.countMembersByRoomId(roomId));
        return dto;
    }

    public boolean isUserInRoom(Long roomId, Long userId) {
        return chatRoomRepository.isUserInRoom(roomId, userId);
    }

    private User getCurrentUser() {
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", principal.getId()));
    }
}
