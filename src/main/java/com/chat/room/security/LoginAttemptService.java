package com.chat.room.security;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks failed login attempts per username and locks accounts
 * when the configured maximum is exceeded.
 * Uses a single ConcurrentHashMap whose values encode both the attempt
 * count (positive) and the lock-until timestamp (negative) to allow
 * fully atomic updates via ConcurrentHashMap.compute().
 *
 * Encoding:
 *   value > 0  → number of failed attempts so far, account not locked
 *   value < 0  → account locked; absolute value is the lock-expiry epoch ms
 */
@Component
public class LoginAttemptService {

    private final ConcurrentHashMap<String, Long> state = new ConcurrentHashMap<>();

    /**
     * Record a successful login — resets the attempt counter for this user.
     */
    public void loginSucceeded(String username) {
        state.remove(username);
    }

    /**
     * Record a failed login attempt.
     *
     * @param username       the username that failed to authenticate
     * @param maxAttempts    max failures before locking
     * @param lockDurationMs duration of the lock in milliseconds
     */
    public void loginFailed(String username, int maxAttempts, long lockDurationMs) {
        state.compute(username, (k, current) -> {
            long attempts = (current == null || current < 0) ? 1L : current + 1L;
            if (attempts >= maxAttempts) {
                // Encode lock expiry as a negative value
                return -(System.currentTimeMillis() + lockDurationMs);
            }
            return attempts;
        });
    }

    /**
     * Returns true if the account is currently locked out.
     */
    public boolean isLocked(String username) {
        Long value = state.get(username);
        if (value == null || value >= 0) {
            return false;
        }
        long lockedUntil = -value;
        if (System.currentTimeMillis() > lockedUntil) {
            state.remove(username);
            return false;
        }
        return true;
    }
}
