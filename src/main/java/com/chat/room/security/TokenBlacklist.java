package com.chat.room.security;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory store of revoked JWT tokens.
 * Tokens are automatically evicted once their expiry time has passed.
 */
@Component
public class TokenBlacklist {

    // Maps token -> expiry time in milliseconds
    private final Map<String, Long> blacklist = new ConcurrentHashMap<>();

    /**
     * Add a token to the blacklist until its natural expiry time.
     */
    public void revoke(String token, Date expiresAt) {
        if (token != null && expiresAt != null) {
            blacklist.put(token, expiresAt.getTime());
        }
    }

    /**
     * Returns true if the token has been revoked and is still within its original validity window.
     */
    public boolean isRevoked(String token) {
        Long expiresAt = blacklist.get(token);
        if (expiresAt == null) {
            return false;
        }
        if (System.currentTimeMillis() > expiresAt) {
            // Token has naturally expired; clean it up
            blacklist.remove(token);
            return false;
        }
        return true;
    }

    /**
     * Removes all entries whose expiry time is in the past.
     * Runs every hour to prevent unbounded memory growth.
     */
    @Scheduled(fixedRate = 3_600_000)
    public void evictExpired() {
        long now = System.currentTimeMillis();
        blacklist.entrySet().removeIf(entry -> entry.getValue() < now);
    }
}
