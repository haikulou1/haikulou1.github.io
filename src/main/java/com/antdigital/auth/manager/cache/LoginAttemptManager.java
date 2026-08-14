package com.antdigital.auth.manager.cache;

import com.antdigital.auth.common.constant.AuthConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 登录尝试管理器，负责失败次数统计、账号锁定与 IP 限流。
 *
 * <p>当前实现基于内存，适用于单机演示；生产环境应替换为 Redis 实现，
 * 以支持分布式限流与跨实例的失败计数。</p>
 */
@Component
public class LoginAttemptManager {

    private static final Logger logger = LoggerFactory.getLogger(LoginAttemptManager.class);

    private final Map<String, Instant> lockStore = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> ipAttemptStore = new ConcurrentHashMap<>();
    private final Map<String, Instant> ipWindowStart = new ConcurrentHashMap<>();

    @Value("${auth.login.max-fail-count:5}")
    private int maxFailCount;

    @Value("${auth.login.lock-duration-seconds:900}")
    private long lockDurationSeconds;

    @Value("${auth.login.ip-rate-limit-per-minute:20}")
    private int ipRateLimitPerMinute;

    @Value("${auth.login.captcha-threshold:3}")
    private int captchaThreshold;

    /**
     * 判断账号是否处于锁定状态。
     *
     * @param username 用户名
     * @return true 表示已锁定
     */
    public boolean isLocked(String username) {
        Instant lockUntil = lockStore.get(username);
        if (lockUntil == null) {
            return false;
        }
        if (Instant.now().isAfter(lockUntil)) {
            lockStore.remove(username);
            return false;
        }
        return true;
    }

    /**
     * 获取账号剩余锁定秒数。
     *
     * @param username 用户名
     * @return 剩余秒数，未锁定返回 0
     */
    public long getRemainingLockSeconds(String username) {
        Instant lockUntil = lockStore.get(username);
        if (lockUntil == null) {
            return 0L;
        }
        long remaining = Duration.between(Instant.now(), lockUntil).getSeconds();
        return Math.max(0L, remaining);
    }

    /**
     * 记录一次登录失败，达到阈值后锁定账号。
     *
     * @param username 用户名
     * @return 锁定后是否仍处于锁定状态
     */
    public boolean recordFailure(String username) {
        // 此处失败次数由 UserMapper 持久化，本方法负责锁定判定
        // 调用方传入已累计的失败次数
        return isLocked(username);
    }

    /**
     * 根据累计失败次数判定并执行锁定。
     *
     * @param username     用户名
     * @param currentFails 当前累计失败次数
     * @return 是否触发了锁定
     */
    public boolean applyLockIfExceeded(String username, int currentFails) {
        if (currentFails >= maxFailCount) {
            lockStore.put(username, Instant.now().plusSeconds(lockDurationSeconds));
            logger.warn("账号已锁定, username: {}, failCount: {}, lockSeconds: {}",
                    username, currentFails, lockDurationSeconds);
            return true;
        }
        return false;
    }

    /**
     * 重置账号失败状态（登录成功后调用）。
     *
     * @param username 用户名
     */
    public void resetFailures(String username) {
        lockStore.remove(username);
    }

    /**
     * 判断是否需要验证码。
     *
     * @param currentFails 当前累计失败次数
     * @return true 表示需要验证码
     */
    public boolean isCaptchaRequired(int currentFails) {
        return currentFails >= captchaThreshold;
    }

    /**
     * IP 限流检查：每分钟内尝试次数是否超限。
     *
     * @param ip 客户端IP
     * @return true 表示已被限流
     */
    public boolean isIpRateLimited(String ip) {
        if (ip == null || ip.isBlank()) {
            return false;
        }
        Instant windowStart = ipWindowStart.get(ip);
        Instant now = Instant.now();
        if (windowStart == null || now.isAfter(windowStart.plusSeconds(60))) {
            ipWindowStart.put(ip, now);
            ipAttemptStore.put(ip, new AtomicInteger(1));
            return false;
        }
        AtomicInteger count = ipAttemptStore.computeIfAbsent(ip, k -> new AtomicInteger(0));
        return count.incrementAndGet() > ipRateLimitPerMinute;
    }

    /**
     * 构造锁定提示信息。
     *
     * @param username 用户名
     * @return 提示文案
     */
    public String buildLockedTip(String username) {
        long remaining = getRemainingLockSeconds(username);
        long minutes = (remaining + AuthConstants.SECONDS_PER_MINUTE - 1)
                / AuthConstants.SECONDS_PER_MINUTE;
        return AuthConstants.MSG_ACCOUNT_LOCKED_PREFIX + minutes + "分钟后重试";
    }

    /**
     * 获取锁定时长（秒）。
     *
     * @return 锁定时长秒数
     */
    public long getLockDurationSeconds() {
        return lockDurationSeconds;
    }

    /**
     * 获取最大失败次数（测试与监控用）。
     *
     * @return 最大失败次数
     */
    public int getMaxFailCount() {
        return maxFailCount;
    }

    /**
     * 获取验证码阈值（测试用）。
     *
     * @return 验证码阈值
     */
    public int getCaptchaThreshold() {
        return captchaThreshold;
    }
}
