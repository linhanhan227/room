package com.chat.room.websocket;

import com.chat.room.dto.UserDTO;
import com.chat.room.entity.User;
import com.chat.room.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final UserService userService;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        log.info("WebSocket connection established: {}", headerAccessor.getSessionId());
        
        if (headerAccessor.getUser() != null) {
            try {
                String username = headerAccessor.getUser().getName();
                UserDTO user = userService.getUserByUsername(username);
                if (user != null) {
                    userService.updateUserStatus(user.getId(), User.UserStatus.ONLINE);
                    
                    messagingTemplate.convertAndSend("/topic/user.status", Map.of(
                            "userId", user.getId(),
                            "status", "ONLINE",
                            "username", user.getUsername()
                    ));
                }
            } catch (Exception e) {
                log.error("Error handling WebSocket connect: {}", e.getMessage());
            }
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        log.info("WebSocket connection closed: {}", headerAccessor.getSessionId());
        
        if (headerAccessor.getUser() != null) {
            try {
                String username = headerAccessor.getUser().getName();
                UserDTO user = userService.getUserByUsername(username);
                if (user != null) {
                    userService.updateUserStatus(user.getId(), User.UserStatus.OFFLINE);
                    
                    messagingTemplate.convertAndSend("/topic/user.status", Map.of(
                            "userId", user.getId(),
                            "status", "OFFLINE",
                            "username", user.getUsername()
                    ));
                }
            } catch (Exception e) {
                log.error("Error handling WebSocket disconnect: {}", e.getMessage());
            }
        }
    }
}
