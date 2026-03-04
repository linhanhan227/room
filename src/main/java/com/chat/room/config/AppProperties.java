package com.chat.room.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Jwt jwt = new Jwt();
    private Email email = new Email();
    private SensitiveWord sensitiveWord = new SensitiveWord();
    private WebSocket webSocket = new WebSocket();
    private Cors cors = new Cors();
    private ChatRoom chatRoom = new ChatRoom();
    private FileUpload fileUpload = new FileUpload();
    private Security security = new Security();
    private RoomRecommendation roomRecommendation = new RoomRecommendation();

    @Data
    public static class Jwt {
        private String secret = "your-256-bit-secret-key-for-jwt-token-generation-must-be-long-enough";
        private Long expiration = 86400000L;
        private String header = "Authorization";
        private String prefix = "Bearer ";
    }

    @Data
    public static class Email {
        private String from = "noreply@example.com";
        private Verification verification = new Verification();
        private DailyLimit dailyLimit = new DailyLimit();

        @Data
        public static class Verification {
            private String subject = "邮箱验证码 - 聊天室";
            private int codeExpiration = 300;
            private int codeLength = 6;
            private int maxSendPerHour = 5;
            private int sendIntervalSeconds = 60;
        }

        @Data
        public static class DailyLimit {
            private boolean enabled = true;
            private int maxCount = 10;
            private int resetHour = 0;
        }
    }

    @Data
    public static class SensitiveWord {
        private String filePath = "sensitive_words.txt";
        private boolean enabled = true;
        private String algorithm = "AC";
        private String replacement = "***";
        private boolean autoReload = true;
        private long reloadInterval = 300000;
    }

    @Data
    public static class WebSocket {
        private String endpoint = "/ws";
        private boolean sockJsEnabled = true;  // 是否启用SockJS（false则使用原生WebSocket）
        private long heartbeatInterval = 30000;
        private long heartbeatTimeout = 90000;
        private int messageSizeLimit = 131072;
        private int sendBufferSizeLimit = 524288;
        private int sendTimeLimit = 20000;
        private int timeToFirstMessage = 30000;
        private String[] allowedOrigins = {"*"};
    }

    @Data
    public static class Cors {
        private String[] allowedOriginPatterns = {"*"};
        private String[] allowedMethods = {"GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"};
        private String[] allowedHeaders = {"*"};
        private boolean allowCredentials = true;
        private long maxAge = 3600;
    }

    @Data
    public static class ChatRoom {
        private int maxMembers = 100;
        private int maxRoomsPerUser = 10;
        private int messagePageSize = 50;
        private int maxMessageLength = 5000;
        private boolean allowPrivateRooms = true;
        private boolean allowRoomPassword = true;
    }

    @Data
    public static class FileUpload {
        private long maxSize = 10485760;
        private String[] allowedTypes = {"image/jpeg", "image/png", "image/gif", "image/webp"};
        private String uploadPath = "uploads";
        private boolean enableImageUpload = false;
    }

    @Data
    public static class Security {
        private int passwordMinLength = 6;
        private int passwordMaxLength = 20;
        private boolean passwordRequireUppercase = false;
        private boolean passwordRequireLowercase = true;
        private boolean passwordRequireDigit = false;
        private boolean passwordRequireSpecial = false;
        private int usernameMinLength = 3;
        private int usernameMaxLength = 20;
        private int loginMaxAttempts = 5;
        private long loginLockDuration = 1800000;
    }

    @Data
    public static class RoomRecommendation {
        private boolean enabled = true;
        private String defaultStrategy = "HYBRID";
        private int recommendationLimit = 10;
        private int cacheDuration = 300;
        private HybridWeights hybridWeights = new HybridWeights();

        @Data
        public static class HybridWeights {
            private double activityWeight = 0.3;
            private double popularityWeight = 0.3;
            private double newestWeight = 0.2;
            private double randomWeight = 0.2;
        }
    }

    public List<String> getAllowedOriginPatternsList() {
        return Arrays.asList(cors.getAllowedOriginPatterns());
    }

    public List<String> getAllowedMethodsList() {
        return Arrays.asList(cors.getAllowedMethods());
    }

    public List<String> getAllowedHeadersList() {
        return Arrays.asList(cors.getAllowedHeaders());
    }
}
