package com.chat.room.websocket;

import com.chat.room.dto.MessageDTO;
import com.chat.room.dto.SendMessageRequest;
import com.chat.room.dto.UserDTO;
import com.chat.room.entity.Message;
import com.chat.room.entity.User;
import com.chat.room.exception.BusinessException;
import com.chat.room.service.ChatRoomService;
import com.chat.room.service.MessageService;
import com.chat.room.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class WebSocketChatHandler {

    private final SimpMessagingTemplate messagingTemplate;
    private final MessageService messageService;
    private final ChatRoomService chatRoomService;
    private final UserService userService;
    private final WebSocketEventListener webSocketEventListener;

    @MessageMapping("/chat.send.{roomId}")
    public void sendMessage(@DestinationVariable Long roomId, @Payload SendMessageRequest request,
                           SimpMessageHeaderAccessor headerAccessor) {
        try {
            User user = getUserFromSession(headerAccessor);
            if (user == null) {
                log.warn("Unauthorized message attempt to room: {}", roomId);
                return;
            }

            if (!chatRoomService.isUserInRoom(roomId, user.getId())) {
                log.warn("User {} not in room {}", user.getId(), roomId);
                sendErrorToUser(headerAccessor, "您不是该聊天室的成员");
                return;
            }

            webSocketEventListener.updateHeartbeat(user.getId());

            request.setRoomId(roomId);
            MessageDTO message = messageService.saveMessage(roomId, user.getId(), request.getContent(),
                    request.getType() != null ? Message.MessageType.valueOf(request.getType().toUpperCase()) : Message.MessageType.TEXT);

            messagingTemplate.convertAndSend("/topic/room." + roomId, message);
            log.debug("Message sent to room {}: {}", roomId, message.getContent());
        } catch (BusinessException e) {
            log.warn("Business error sending message to room {}: {}", roomId, e.getMessage());
            sendErrorToUser(headerAccessor, e.getMessage());
        } catch (Exception e) {
            log.error("Error sending message to room {}: {}", roomId, e.getMessage());
            sendErrorToUser(headerAccessor, "消息发送失败");
        }
    }

    @MessageMapping("/chat.join.{roomId}")
    public void joinRoom(@DestinationVariable Long roomId, SimpMessageHeaderAccessor headerAccessor) {
        try {
            User user = getUserFromSession(headerAccessor);
            if (user != null) {
                webSocketEventListener.updateHeartbeat(user.getId());

                MessageDTO systemMessage = MessageDTO.builder()
                        .roomId(roomId)
                        .senderId(0L)
                        .senderName("System")
                        .content(user.getNickname() + " joined the room")
                        .type(Message.MessageType.SYSTEM)
                        .build();

                messagingTemplate.convertAndSend("/topic/room." + roomId, systemMessage);
                log.info("User {} joined room {}", user.getId(), roomId);
            }
        } catch (Exception e) {
            log.error("Error joining room {}: {}", roomId, e.getMessage());
        }
    }

    @MessageMapping("/chat.leave.{roomId}")
    public void leaveRoom(@DestinationVariable Long roomId, SimpMessageHeaderAccessor headerAccessor) {
        try {
            User user = getUserFromSession(headerAccessor);
            if (user != null) {
                webSocketEventListener.updateHeartbeat(user.getId());

                MessageDTO systemMessage = MessageDTO.builder()
                        .roomId(roomId)
                        .senderId(0L)
                        .senderName("System")
                        .content(user.getNickname() + " left the room")
                        .type(Message.MessageType.SYSTEM)
                        .build();

                messagingTemplate.convertAndSend("/topic/room." + roomId, systemMessage);
                log.info("User {} left room {}", user.getId(), roomId);
            }
        } catch (Exception e) {
            log.error("Error leaving room {}: {}", roomId, e.getMessage());
        }
    }

    @MessageMapping("/chat.typing.{roomId}")
    public void typingIndicator(@DestinationVariable Long roomId, SimpMessageHeaderAccessor headerAccessor) {
        try {
            User user = getUserFromSession(headerAccessor);
            if (user != null) {
                webSocketEventListener.updateHeartbeat(user.getId());

                Map<String, Object> typingEvent = Map.of(
                        "userId", user.getId(),
                        "username", user.getNickname() != null ? user.getNickname() : user.getUsername(),
                        "typing", true
                );
                messagingTemplate.convertAndSend("/topic/room." + roomId + ".typing", typingEvent);
            }
        } catch (Exception e) {
            log.error("Error sending typing indicator: {}", e.getMessage());
        }
    }

    @MessageMapping("/user.status")
    public void updateStatus(@Payload Map<String, String> statusUpdate, SimpMessageHeaderAccessor headerAccessor) {
        try {
            User user = getUserFromSession(headerAccessor);
            if (user != null) {
                webSocketEventListener.updateHeartbeat(user.getId());

                String status = statusUpdate.get("status");
                if (status != null) {
                    userService.updateUserStatus(user.getId(), User.UserStatus.valueOf(status.toUpperCase()));
                    
                    Map<String, Object> statusEvent = Map.of(
                            "userId", user.getId(),
                            "status", status
                    );
                    messagingTemplate.convertAndSend("/topic/user.status", statusEvent);
                }
            }
        } catch (Exception e) {
            log.error("Error updating status: {}", e.getMessage());
        }
    }

    private User getUserFromSession(SimpMessageHeaderAccessor headerAccessor) {
        try {
            // 优先从 HTTP 握手阶段存入的 session attributes 获取用户信息（避免数据库查询）
            Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();
            if (sessionAttributes != null && sessionAttributes.containsKey("userId")) {
                Long userId = (Long) sessionAttributes.get("userId");
                String username = (String) sessionAttributes.get("username");
                String nickname = (String) sessionAttributes.get("nickname");
                return User.builder()
                        .id(userId)
                        .username(username)
                        .nickname(nickname)
                        .build();
            }

            // 降级：从 STOMP 认证的 Principal 中获取（需查数据库）
            if (headerAccessor.getUser() != null) {
                String username = headerAccessor.getUser().getName();
                UserDTO userDTO = userService.getUserByUsername(username);
                if (userDTO != null) {
                    return User.builder()
                            .id(userDTO.getId())
                            .username(userDTO.getUsername())
                            .nickname(userDTO.getNickname())
                            .avatar(userDTO.getAvatar())
                            .role(userDTO.getRole())
                            .status(userDTO.getStatus())
                            .build();
                }
            }
        } catch (Exception e) {
            log.error("Error getting user from session: {}", e.getMessage());
        }
        return null;
    }

    private void sendErrorToUser(SimpMessageHeaderAccessor headerAccessor, String errorMessage) {
        try {
            if (headerAccessor.getUser() != null) {
                messagingTemplate.convertAndSendToUser(
                        headerAccessor.getUser().getName(),
                        "/queue/errors",
                        Map.of("error", errorMessage, "timestamp", System.currentTimeMillis())
                );
            }
        } catch (Exception e) {
            log.error("Error sending error message to user: {}", e.getMessage());
        }
    }
}
