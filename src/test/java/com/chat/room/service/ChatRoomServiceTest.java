package com.chat.room.service;

import com.chat.room.dto.ChatRoomDTO;
import com.chat.room.dto.CreateRoomRequest;
import com.chat.room.entity.ChatRoom;
import com.chat.room.entity.RoomMember;
import com.chat.room.entity.User;
import com.chat.room.exception.BusinessException;
import com.chat.room.exception.ForbiddenException;
import com.chat.room.repository.ChatRoomRepository;
import com.chat.room.repository.MessageRepository;
import com.chat.room.repository.RoomMemberRepository;
import com.chat.room.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatRoomServiceTest {

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoomMemberRepository roomMemberRepository;

    @Mock
    private MessageRepository messageRepository;

    @InjectMocks
    private ChatRoomService chatRoomService;

    private User testUser;
    private ChatRoom testRoom;
    private CreateRoomRequest createRoomRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .nickname("Test User")
                .rooms(new HashSet<>())
                .build();

        testRoom = ChatRoom.builder()
                .id(1L)
                .name("Test Room")
                .description("Test Description")
                .owner(testUser)
                .type(ChatRoom.RoomType.PUBLIC)
                .status(ChatRoom.RoomStatus.ACTIVE)
                .members(new HashSet<>())
                .build();

        createRoomRequest = CreateRoomRequest.builder()
                .name("Test Room")
                .description("Test Description")
                .type("PUBLIC")
                .build();

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @DisplayName("Should create room successfully")
    void createRoom_Success() {
        when(chatRoomRepository.existsByName(anyString())).thenReturn(false);
        when(chatRoomRepository.save(any(ChatRoom.class))).thenReturn(testRoom);
        when(roomMemberRepository.save(any(RoomMember.class))).thenReturn(null);

        mockCurrentUser();

        ChatRoomDTO result = chatRoomService.createRoom(createRoomRequest);

        assertNotNull(result);
        assertEquals("Test Room", result.getName());

        verify(chatRoomRepository).save(any(ChatRoom.class));
        verify(roomMemberRepository).save(any(RoomMember.class));
    }

    @Test
    @DisplayName("Should throw exception when room name exists")
    void createRoom_NameExists() {
        when(chatRoomRepository.existsByName("Test Room")).thenReturn(true);

        mockCurrentUser();

        assertThrows(BusinessException.class, () -> chatRoomService.createRoom(createRoomRequest));

        verify(chatRoomRepository, never()).save(any(ChatRoom.class));
    }

    @Test
    @DisplayName("Should get room by id")
    void getRoomById_Success() {
        when(chatRoomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(chatRoomRepository.countMembersByRoomId(1L)).thenReturn(5);

        ChatRoomDTO result = chatRoomService.getRoomById(1L);

        assertNotNull(result);
        assertEquals("Test Room", result.getName());
        assertEquals(5, result.getMemberCount());
    }

    @Test
    @DisplayName("Should delete room successfully")
    void deleteRoom_Success() {
        when(chatRoomRepository.findById(1L)).thenReturn(Optional.of(testRoom));

        mockCurrentUser();

        assertDoesNotThrow(() -> chatRoomService.deleteRoom(1L));

        verify(messageRepository).deleteByRoomId(1L);
        verify(roomMemberRepository).deleteByRoomId(1L);
        verify(chatRoomRepository).delete(testRoom);
    }

    @Test
    @DisplayName("Should throw forbidden when non-owner tries to delete")
    void deleteRoom_NotOwner() {
        User otherUser = User.builder().id(2L).username("other").build();
        testRoom.setOwner(otherUser);
        when(chatRoomRepository.findById(1L)).thenReturn(Optional.of(testRoom));

        mockCurrentUser();

        assertThrows(ForbiddenException.class, () -> chatRoomService.deleteRoom(1L));

        verify(chatRoomRepository, never()).delete(any(ChatRoom.class));
    }

    @Test
    @DisplayName("Should get public rooms")
    void getPublicRooms_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        List<ChatRoom> rooms = List.of(testRoom);
        Page<ChatRoom> roomPage = new PageImpl<>(rooms, pageable, 1);

        when(chatRoomRepository.findPublicRooms(pageable)).thenReturn(roomPage);
        when(chatRoomRepository.countMembersByRoomId(1L)).thenReturn(5);

        Page<ChatRoomDTO> result = chatRoomService.getPublicRooms(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    private void mockCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        when(authentication.getPrincipal()).thenReturn(
                new org.springframework.security.core.userdetails.User(
                        "testuser", "password", List.of()
                )
        );
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    }
}
