package com.chat.room.websocket;

import com.chat.room.dto.UserDTO;
import com.chat.room.entity.User;
import com.chat.room.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final UserService userService;

    private final ConcurrentHashMap<String, Long> sessionUserMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, Long> userLastHeartbeat = new ConcurrentHashMap<>();

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        log.info("WebSocket connection established: {}", headerAccessor.getSessionId());
        
        if (headerAccessor.getUser() != null) {
            try {
                String username = headerAccessor.getUser().getName();
                UserDTO user = userService.getUserByUsername(username);
                if (user != null) {
                    sessionUserMap.put(headerAccessor.getSessionId(), user.getId());
                    userLastHeartbeat.put(user.getId(), System.currentTimeMillis());
                    
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
                    sessionUserMap.remove(headerAccessor.getSessionId());
                    userLastHeartbeat.remove(user.getId());
                    
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

    @MessageMapping("/heartbeat")
    public void handleHeartbeat(StompHeaderAccessor headerAccessor) {
        if (headerAccessor.getUser() != null) {
            try {
                String username = headerAccessor.getUser().getName();
                UserDTO user = userService.getUserByUsername(username);
                if (user != null) {
                    userLastHeartbeat.put(user.getId(), System.currentTimeMillis());
                    log.debug("Heartbeat received from user: {}", user.getId());
                }
            } catch (Exception e) {
                log.error("Error handling heartbeat: {}", e.getMessage());
            }
        }
    }

    @Scheduled(fixedRate = 60000)
    public void checkHeartbeats() {
        long currentTime = System.currentTimeMillis();
        long timeout = 90000;
        
        userLastHeartbeat.forEach((userId, lastHeartbeat) -> {
            if (currentTime - lastHeartbeat > timeout) {
                log.warn("User {} heartbeat timeout, last heartbeat: {}", userId, lastHeartbeat);
                try {
                    userService.updateUserStatus(userId, User.UserStatus.OFFLINE);
                    messagingTemplate.convertAndSend("/topic/user.status", Map.of(
                            "userId", userId,
                            "status", "OFFLINE"
                    ));
                    userLastHeartbeat.remove(userId);
                } catch (Exception e) {
                    log.error("Error handling heartbeat timeout: {}", e.getMessage());
                }
            }
        });
    }

    public void updateHeartbeat(Long userId) {
        userLastHeartbeat.put(userId, System.currentTimeMillis());
    }

    public Long getLastHeartbeat(Long userId) {
        return userLastHeartbeat.get(userId);
    }
}
