package com.chat.room.security;

import com.chat.room.entity.User;
import com.chat.room.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * WebSocket HTTP 握手拦截器
 * <p>
 * 在 HTTP → WebSocket 协议升级阶段进行预处理：
 * 1. 从请求参数 (token) 或请求头 (Authorization) 中提取 JWT Token
 * 2. 验证 Token 有效性并解析用户信息
 * 3. 将用户信息存入 WebSocket Session attributes，供后续 STOMP 消息处理使用
 * <p>
 * 支持两种 Token 传递方式：
 * - 查询参数：ws://host/api/ws?token=xxx（适用于浏览器原生 WebSocket / SockJS）
 * - 请求头：Authorization: Bearer xxx（适用于支持自定义请求头的客户端）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        String token = extractToken(request);

        if (StringUtils.hasText(token) && tokenProvider.validateToken(token)) {
            String username = tokenProvider.getUsernameFromToken(token);
            User user = userRepository.findByUsername(username).orElse(null);

            if (user != null) {
                attributes.put("userId", user.getId());
                attributes.put("username", user.getUsername());
                attributes.put("nickname", user.getNickname());
                log.debug("WebSocket handshake authenticated for user: {} (ID: {})", username, user.getId());
                return true;
            }

            log.warn("WebSocket handshake failed: user '{}' not found in database", username);
        } else if (StringUtils.hasText(token)) {
            log.warn("WebSocket handshake failed: invalid JWT token");
        } else {
            log.debug("WebSocket handshake without token, authentication will be checked at STOMP CONNECT");
        }

        // 允许无 Token 的握手通过（兼容在 STOMP CONNECT 阶段认证的流程）
        // 如需强制 HTTP 握手阶段认证，将此处改为 return false
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        if (exception != null) {
            log.error("WebSocket handshake error: {}", exception.getMessage());
        }
    }

    /**
     * 从请求中提取 JWT Token
     * <p>
     * 优先级：查询参数 token > 请求头 Authorization
     *
     * @param request HTTP 请求
     * @return JWT Token 字符串，无法提取时返回 null
     */
    private String extractToken(ServerHttpRequest request) {
        // 方式1：从查询参数中提取 token（适用于 WebSocket / SockJS 连接）
        String query = request.getURI().getQuery();
        if (StringUtils.hasText(query)) {
            String[] params = query.split("&");
            for (String param : params) {
                String[] keyValue = param.split("=", 2);
                if (keyValue.length == 2 && "token".equals(keyValue[0])) {
                    return keyValue[1];
                }
            }
        }

        // 方式2：从请求头中提取 Authorization: Bearer xxx
        String authHeader = null;
        if (request instanceof ServletServerHttpRequest servletRequest) {
            authHeader = servletRequest.getServletRequest().getHeader("Authorization");
        } else {
            // 非 Servlet 环境，从通用 Headers 中获取
            var authHeaders = request.getHeaders().get("Authorization");
            if (authHeaders != null && !authHeaders.isEmpty()) {
                authHeader = authHeaders.get(0);
            }
        }

        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        return null;
    }
}
