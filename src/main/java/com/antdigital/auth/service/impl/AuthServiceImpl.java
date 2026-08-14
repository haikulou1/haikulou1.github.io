package com.antdigital.auth.service.impl;

import com.antdigital.auth.common.constant.AuthConstants;
import com.antdigital.auth.common.enums.ResponseCodeEnum;
import com.antdigital.auth.common.exception.BusinessException;
import com.antdigital.auth.dao.mapper.UserMapper;
import com.antdigital.auth.manager.cache.CaptchaManager;
import com.antdigital.auth.manager.cache.LoginAttemptManager;
import com.antdigital.auth.manager.cache.SessionManager;
import com.antdigital.auth.model.entity.UserDO;
import com.antdigital.auth.model.dto.LoginRequest;
import com.antdigital.auth.model.dto.LoginResponse;
import com.antdigital.auth.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 认证服务实现，承载登录核心流程与异常边界处理。
 */
@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserMapper userMapper;
    private final SessionManager sessionManager;
    private final LoginAttemptManager loginAttemptManager;
    private final CaptchaManager captchaManager;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(UserMapper userMapper,
                           SessionManager sessionManager,
                           LoginAttemptManager loginAttemptManager,
                           CaptchaManager captchaManager,
                           PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.sessionManager = sessionManager;
        this.loginAttemptManager = loginAttemptManager;
        this.captchaManager = captchaManager;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public LoginResponse login(LoginRequest request, String clientIp) {
        // 1. IP 限流检查（防暴力破解）
        if (loginAttemptManager.isIpRateLimited(clientIp)) {
            logger.warn("IP 触发限流, ip: {}", clientIp);
            throw new BusinessException(ResponseCodeEnum.IP_RATE_LIMITED,
                    AuthConstants.MSG_IP_RATE_LIMITED);
        }

        String username = request.getUsername();

        // 2. 查询用户
        UserDO user = userMapper.getByUserName(username);

        // 3. 账号不存在：统一返回「账号或密码错误」，避免账号枚举
        if (user == null) {
            logger.warn("登录失败-账号不存在, username: {}", username);
            throw new BusinessException(ResponseCodeEnum.ACCOUNT_OR_PASSWORD_ERROR,
                    AuthConstants.MSG_ACCOUNT_OR_PASSWORD_ERROR);
        }

        // 4. 账号锁定检查（暴力破解锁定）
        if (loginAttemptManager.isLocked(username)) {
            String tip = loginAttemptManager.buildLockedTip(username);
            logger.warn("登录失败-账号已锁定, username: {}", username);
            throw new BusinessException(ResponseCodeEnum.ACCOUNT_LOCKED, tip);
        }

        // 5. 账号禁用检查
        if (user.getStatus() != null
                && user.getStatus() == AuthConstants.USER_STATUS_DISABLED) {
            logger.warn("登录失败-账号已禁用, username: {}", username);
            throw new BusinessException(ResponseCodeEnum.ACCOUNT_DISABLED,
                    AuthConstants.MSG_ACCOUNT_DISABLED);
        }

        // 6. 验证码检查（失败次数超过阈值后必填）
        int currentFails = user.getFailCount() == null ? 0 : user.getFailCount();
        if (loginAttemptManager.isCaptchaRequired(currentFails)) {
            if (request.getCaptcha() == null || request.getCaptcha().isBlank()
                    || request.getCaptchaId() == null) {
                throw new BusinessException(ResponseCodeEnum.CAPTCHA_REQUIRED,
                        AuthConstants.MSG_CAPTCHA_REQUIRED);
            }
            if (!captchaManager.validate(request.getCaptchaId(), request.getCaptcha())) {
                throw new BusinessException(ResponseCodeEnum.CAPTCHA_ERROR,
                        AuthConstants.MSG_CAPTCHA_ERROR);
            }
        }

        // 7. 密码校验（BCrypt 加盐哈希）
        boolean passwordMatched = passwordEncoder.matches(
                request.getPassword(), user.getPasswordHash());

        // 8. 密码错误：累计失败次数，达到阈值则锁定，统一返回防枚举提示
        if (!passwordMatched) {
            int newFailCount = currentFails + 1;
            boolean locked = loginAttemptManager.applyLockIfExceeded(username, newFailCount);
            LocalDateTime lockUntil = locked
                    ? LocalDateTime.now().plusSeconds(loginAttemptManager.getLockDurationSeconds())
                    : null;
            userMapper.updateFailStatus(user.getId(), newFailCount, lockUntil);
            if (locked) {
                logger.warn("登录失败-触发锁定, username: {}, failCount: {}", username, newFailCount);
                throw new BusinessException(ResponseCodeEnum.ACCOUNT_LOCKED,
                        loginAttemptManager.buildLockedTip(username));
            }
            logger.warn("登录失败-密码错误, username: {}, failCount: {}", username, newFailCount);
            throw new BusinessException(ResponseCodeEnum.ACCOUNT_OR_PASSWORD_ERROR,
                    AuthConstants.MSG_ACCOUNT_OR_PASSWORD_ERROR);
        }

        // 9. 登录成功：重置失败状态，创建会话
        userMapper.resetFailStatus(user.getId());
        loginAttemptManager.resetFailures(username);

        boolean rememberMe = Boolean.TRUE.equals(request.getRememberMe());
        String token = sessionManager.createSession(user.getId(), username, rememberMe);
        long ttl = sessionManager.getSessionTtl(rememberMe);

        logger.info("登录成功, username: {}, rememberMe: {}", username, rememberMe);
        return new LoginResponse(token, username, ttl);
    }

    @Override
    public void logout(String token) {
        if (token == null || token.isBlank()) {
            return;
        }
        sessionManager.invalidate(token);
        logger.info("退出登录成功, token: {}", token);
    }
}
