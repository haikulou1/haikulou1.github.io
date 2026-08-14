package com.antdigital.auth.manager.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 会话管理器，维护登录态。
 *
 * <p>当前实现基于内存 ConcurrentHashMap + TTL，适用于单机演示；
 * 生产环境应替换为 Redis 实现，以保证分布式会话与水平扩展。</p>
 */
@Component
public class SessionManager {

    private static final Logger logger = LoggerFactory.getLogger(SessionManager.class);

    private final Map<String, SessionEntry> sessionStore = new ConcurrentHashMap<>();

    @Value("${auth.login.session-ttl-seconds:1800}")
    private long sessionTtlSeconds;

    @Value("${auth.login.remember-me-ttl-seconds:604800}")
    private long rememberMeTtlSeconds;

    /**
     * 会话条目。
     */
    private static final class SessionEntry {
        private final String token;
        private final Long userId;
        private final String username;
        private final Instant expireAt;

        SessionEntry(String token, Long userId, String username, Instant expireAt) {
            this.token = token;
            this.userId = userId;
            this.username = username;
            this.expireAt = expireAt;
        }

        boolean isExpired() {
            return Instant.now().isAfter(expireAt);
        }
    }

    /**
     * 创建会话。
     *
     * @param userId     用户ID
     * @param username   用户名
     * @param rememberMe 是否记住我
     * @return 会话令牌
     */
    public String createSession(Long userId, String username, boolean rememberMe) {
        String token = generateToken();
        long ttl = rememberMe ? rememberMeTtlSeconds : sessionTtlSeconds;
        Instant expireAt = Instant.now().plusSeconds(ttl);
        sessionStore.put(token, new SessionEntry(token, userId, username, expireAt));
        logger.info("会话创建成功, userId: {}, rememberMe: {}", userId, rememberMe);
        return token;
    }

    /**
     * 校验会话有效性。
     *
     * @param token 会话令牌
     * @return 用户ID，无效时返回 empty
     */
    public Optional<Long> validateSession(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        SessionEntry entry = sessionStore.get(token);
        if (entry == null) {
            return Optional.empty();
        }
        if (entry.isExpired()) {
            sessionStore.remove(token);
            return Optional.empty();
        }
        return Optional.of(entry.userId);
    }

    /**
     * 获取会话有效期（秒）。
     *
     * @param rememberMe 是否记住我
     * @return 有效期秒数
     */
    public long getSessionTtl(boolean rememberMe) {
        return rememberMe ? rememberMeTtlSeconds : sessionTtlSeconds;
    }

    /**
     * 注销会话。
     *
     * @param token 会话令牌
     */
    public void invalidate(String token) {
        if (token != null) {
            sessionStore.remove(token);
            logger.info("会话已注销, token: {}", token);
        }
    }

    private String generateToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 清理过期会话（定时任务可调用）。
     */
    public void cleanupExpiredSessions() {
        Instant now = Instant.now();
        sessionStore.entrySet().removeIf(entry -> now.isAfter(entry.getValue().expireAt));
    }

    /**
     * 获取会话剩余有效期（秒），用于测试与监控。
     *
     * @param token 会话令牌
     * @return 剩余秒数，不存在或已过期返回 0
     */
    public long getRemainingSeconds(String token) {
        SessionEntry entry = sessionStore.get(token);
        if (entry == null || entry.isExpired()) {
            return 0L;
        }
        return Math.max(0L, Duration.between(Instant.now(), entry.expireAt).getSeconds());
    }
}
