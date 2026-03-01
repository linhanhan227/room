package com.chat.room.config;

import com.chat.room.security.WebSocketAuthChannelInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketAuthChannelInterceptor webSocketAuthChannelInterceptor;
    private final AppProperties appProperties;

    @Bean
    public TaskScheduler heartBeatScheduler() {
        return new ThreadPoolTaskScheduler();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        long heartbeatInterval = appProperties.getWebSocket().getHeartbeatInterval();
        config.enableSimpleBroker("/topic", "/queue")
                .setHeartbeatValue(new long[]{heartbeatInterval, heartbeatInterval})
                .setTaskScheduler(heartBeatScheduler());
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        long heartbeatInterval = appProperties.getWebSocket().getHeartbeatInterval();
        String endpoint = appProperties.getWebSocket().getEndpoint();
        
        registry.addEndpoint(endpoint)
                .setAllowedOriginPatterns(appProperties.getWebSocket().getAllowedOrigins())
                .withSockJS()
                .setHeartbeatTime(heartbeatInterval);
        
        registry.addEndpoint(endpoint)
                .setAllowedOriginPatterns(appProperties.getWebSocket().getAllowedOrigins());
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(webSocketAuthChannelInterceptor);
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        AppProperties.WebSocket ws = appProperties.getWebSocket();
        registration.setMessageSizeLimit(ws.getMessageSizeLimit());
        registration.setSendBufferSizeLimit(ws.getSendBufferSizeLimit());
        registration.setSendTimeLimit(ws.getSendTimeLimit());
        registration.setTimeToFirstMessage(ws.getTimeToFirstMessage());
    }
}
