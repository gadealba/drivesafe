package com.termProject.drivesafe.session;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Singleton that owns every logged-in user's session.
 *
 * Singleton pattern:
 *  - the constructor is private, so nothing else can create a SessionManager;
 *  - the only instance is created lazily and thread-safely by the JVM the first
 *    time getInstance() is called (initialization-on-demand holder idiom);
 *  - everyone (filter, controllers, tests) reaches the same instance through getInstance().
 *
 * Security measures:
 *  - tokens are 256 random bits from SecureRandom, so they cannot be guessed;
 *  - only a SHA-256 hash of each token is kept in memory, so reading the map
 *    does not reveal a usable token;
 *  - a session ends after 30 minutes without use, or 8 hours after it started;
 *  - every login or sign up gets a brand new token (no session fixation);
 *  - logout removes the session on the server, so a copied token stops working.
 *
 * Limitation: sessions live in memory. They are lost when the app restarts,
 * and a second server instance would have its own separate set.
 */
public final class SessionManager {

    static final Duration IDLE_TIMEOUT = Duration.ofMinutes(30);
    static final Duration ABSOLUTE_TIMEOUT = Duration.ofHours(8);
    private static final int TOKEN_BYTES = 32;

    /** The data kept for one logged-in user. Immutable: touching a session makes a new copy. */
    public record UserSession(long userId, String username, Instant createdAt, Instant lastAccess) {

        private boolean isExpired(Instant now) {
            return now.isAfter(createdAt.plus(ABSOLUTE_TIMEOUT))
                || now.isAfter(lastAccess.plus(IDLE_TIMEOUT));
        }

        private UserSession touched(Instant now) {
            return new UserSession(userId, username, createdAt, now);
        }
    }

    /** Loaded by the JVM on the first getInstance() call, which makes creation lazy and thread-safe. */
    private static final class Holder {
        private static final SessionManager INSTANCE = new SessionManager();
    }

    public static SessionManager getInstance() {
        return Holder.INSTANCE;
    }

    private final SecureRandom random = new SecureRandom();
    private final ConcurrentHashMap<String, UserSession> sessions = new ConcurrentHashMap<>();

    private SessionManager() {}

    /** Starts a session and returns its token. The token is only ever sent to the client's cookie. */
    public String createSession(long userId, String username) {
        purgeExpired();

        byte[] bytes = new byte[TOKEN_BYTES];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        Instant now = Instant.now();
        sessions.put(hash(token), new UserSession(userId, username, now, now));
        return token;
    }

    /**
     * Checks a token. When it is valid, the idle timer restarts and the session is returned.
     * Expired or unknown tokens return empty (and expired ones are removed).
     */
    public Optional<UserSession> validate(String token) {
        if (token == null || token.isBlank()) return Optional.empty();

        Instant now = Instant.now();
        UserSession current = sessions.computeIfPresent(
            hash(token),
            (key, session) -> session.isExpired(now) ? null : session.touched(now));
        return Optional.ofNullable(current);
    }

    /** Ends a session (logout). Unknown tokens are ignored. */
    public void invalidate(String token) {
        if (token == null || token.isBlank()) return;
        sessions.remove(hash(token));
    }

    public int activeSessionCount() {
        purgeExpired();
        return sessions.size();
    }

    private void purgeExpired() {
        Instant now = Instant.now();
        sessions.values().removeIf(session -> session.isExpired(now));
    }

    private static String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}
