package com.chat.room.service;

import com.chat.room.dto.MessageDTO;
import com.chat.room.dto.SendMessageRequest;
import com.chat.room.entity.ChatRoom;
import com.chat.room.entity.Message;
import com.chat.room.entity.User;
import com.chat.room.exception.BusinessException;
import com.chat.room.repository.BannedUserRepository;
import com.chat.room.repository.ChatRoomRepository;
import com.chat.room.repository.MessageRepository;
import com.chat.room.repository.UserRepository;
import com.chat.room.security.UserPrincipal;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BannedUserRepository bannedUserRepository;

    @Mock
    private SensitiveWordService sensitiveWordService;

    @Mock
    private RoomMemberManagementService roomMemberManagementService;

    @InjectMocks
    private MessageService messageService;

    private User testUser;
    private ChatRoom testRoom;
    private Message testMessage;
    private SendMessageRequest sendMessageRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .nickname("Test User")
                .build();

        testRoom = ChatRoom.builder()
                .id(1L)
                .name("Test Room")
                .build();

        testMessage = Message.builder()
                .id(1L)
                .room(testRoom)
                .sender(testUser)
                .content("Hello World")
                .type(Message.MessageType.TEXT)
                .build();

        sendMessageRequest = SendMessageRequest.builder()
                .roomId(1L)
                .content("Hello World")
                .build();

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @DisplayName("Should send message successfully")
    void sendMessage_Success() {
        when(chatRoomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(chatRoomRepository.isUserInRoom(1L, 1L)).thenReturn(true);
        when(messageRepository.save(any(Message.class))).thenReturn(testMessage);

        mockCurrentUser();

        MessageDTO result = messageService.sendMessage(sendMessageRequest);

        assertNotNull(result);
        assertEquals("Hello World", result.getContent());
        assertEquals(1L, result.getRoomId());

        verify(messageRepository).save(any(Message.class));
    }

    @Test
    @DisplayName("Should throw exception when user not in room")
    void sendMessage_UserNotInRoom() {
        when(chatRoomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(chatRoomRepository.isUserInRoom(1L, 1L)).thenReturn(false);

        mockCurrentUser();

        assertThrows(BusinessException.class, () -> messageService.sendMessage(sendMessageRequest));

        verify(messageRepository, never()).save(any(Message.class));
    }

    @Test
    @DisplayName("Should get room messages")
    void getRoomMessages_Success() {
        Pageable pageable = PageRequest.of(0, 20);
        List<Message> messages = List.of(testMessage);
        Page<Message> messagePage = new PageImpl<>(messages, pageable, 1);

        when(chatRoomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(messageRepository.findByRoomIdOrderByCreatedAtDesc(1L, pageable)).thenReturn(messagePage);

        Page<MessageDTO> result = messageService.getRoomMessages(1L, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("Should get recent messages")
    void getRecentMessages_Success() {
        List<Message> messages = List.of(testMessage);

        when(messageRepository.findRecentMessages(eq(1L), any(Pageable.class))).thenReturn(messages);

        List<MessageDTO> result = messageService.getRecentMessages(1L, 50);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should get message count")
    void getMessageCount_Success() {
        when(messageRepository.countByRoomId(1L)).thenReturn(10L);

        Long count = messageService.getMessageCount(1L);

        assertEquals(10L, count);
    }

    private void mockCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal principal = UserPrincipal.create(testUser);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
    }
}
