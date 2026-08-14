package com.antdigital.auth.manager.cache;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 验证码管理器，存储验证码并支持校验。
 *
 * <p>当前实现基于内存，适用于单机演示；生产环境应替换为 Redis 实现。</p>
 */
@Component
public class CaptchaManager {

    private static final long CAPTCHA_TTL_SECONDS = 300L;

    private final Map<String, CaptchaEntry> captchaStore = new ConcurrentHashMap<>();

    private static final class CaptchaEntry {
        private final String text;
        private final Instant expireAt;

        CaptchaEntry(String text, Instant expireAt) {
            this.text = text;
            this.expireAt = expireAt;
        }

        boolean isExpired() {
            return Instant.now().isAfter(expireAt);
        }
    }

    /**
     * 生成验证码。
     *
     * @param captchaId 验证码标识ID（为空时自动生成）
     * @return 验证码标识ID
     */
    public String generate(String captchaId) {
        String id = (captchaId == null || captchaId.isBlank())
                ? UUID.randomUUID().toString().replace("-", "")
                : captchaId;
        String text = String.valueOf((int) ((Math.random() * 9000) + 1000));
        captchaStore.put(id, new CaptchaEntry(text, Instant.now().plusSeconds(CAPTCHA_TTL_SECONDS)));
        return id;
    }

    /**
     * 校验验证码。
     *
     * @param captchaId 验证码标识ID
     * @param input     用户输入的验证码
     * @return true 表示校验通过
     */
    public boolean validate(String captchaId, String input) {
        if (captchaId == null || input == null) {
            return false;
        }
        CaptchaEntry entry = captchaStore.get(captchaId);
        if (entry == null || entry.isExpired()) {
            return false;
        }
        boolean matched = entry.text.equalsIgnoreCase(input);
        // 验证码一次性使用，无论成功失败均移除
        captchaStore.remove(captchaId);
        return matched;
    }
}
