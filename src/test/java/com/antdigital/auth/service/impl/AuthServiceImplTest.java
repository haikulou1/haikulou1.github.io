package com.antdigital.auth.service.impl;

import com.antdigital.auth.common.enums.ResponseCodeEnum;
import com.antdigital.auth.common.exception.BusinessException;
import com.antdigital.auth.dao.mapper.UserMapper;
import com.antdigital.auth.manager.cache.CaptchaManager;
import com.antdigital.auth.manager.cache.LoginAttemptManager;
import com.antdigital.auth.manager.cache.SessionManager;
import com.antdigital.auth.model.entity.UserDO;
import com.antdigital.auth.model.dto.LoginRequest;
import com.antdigital.auth.model.dto.LoginResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link AuthServiceImpl} 单元测试。
 *
 * <p>覆盖正常登录、账号不存在、密码错误、暴力破解锁定、账号禁用、IP 限流、
 * 验证码缺失/错误、记住我区分有效期、退出登录等场景。</p>
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private SessionManager sessionManager;

    @Mock
    private LoginAttemptManager loginAttemptManager;

    @Mock
    private CaptchaManager captchaManager;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    private static final String USERNAME = "alice";
    private static final String PASSWORD = "secret123";
    private static final String CLIENT_IP = "192.168.1.1";
    private static final String TOKEN = "fake-token";
    private static final long SESSION_TTL = 1800L;
    private static final long REMEMBER_ME_TTL = 604800L;

    private LoginRequest buildRequest() {
        LoginRequest request = new LoginRequest();
        request.setUsername(USERNAME);
        request.setPassword(PASSWORD);
        return request;
    }

    private UserDO buildEnabledUser(int failCount) {
        UserDO user = new UserDO();
        user.setId(1L);
        user.setUserName(USERNAME);
        user.setPasswordHash("$2a$10$hashedvalue");
        user.setStatus(1);
        user.setFailCount(failCount);
        return user;
    }

    // ==================== login 测试 ====================

    @Test
    @DisplayName("正常登录：返回令牌与用户名")
    void should_returnToken_when_loginValid() {
        // Arrange
        LoginRequest request = buildRequest();
        UserDO user = buildEnabledUser(0);
        when(loginAttemptManager.isIpRateLimited(CLIENT_IP)).thenReturn(false);
        when(userMapper.getByUserName(USERNAME)).thenReturn(user);
        when(loginAttemptManager.isLocked(USERNAME)).thenReturn(false);
        when(loginAttemptManager.isCaptchaRequired(0)).thenReturn(false);
        when(passwordEncoder.matches(PASSWORD, user.getPasswordHash())).thenReturn(true);
        when(sessionManager.createSession(1L, USERNAME, false)).thenReturn(TOKEN);
        when(sessionManager.getSessionTtl(false)).thenReturn(SESSION_TTL);

        // Act
        LoginResponse response = authService.login(request, CLIENT_IP);

        // Assert
        assertThat(response.getToken()).isEqualTo(TOKEN);
        assertThat(response.getUsername()).isEqualTo(USERNAME);
        assertThat(response.getExpiresIn()).isEqualTo(SESSION_TTL);
        verify(userMapper).resetFailStatus(1L);
        verify(loginAttemptManager).resetFailures(USERNAME);
    }

    @Test
    @DisplayName("记住我：会话使用长有效期")
    void should_useLongTtl_when_rememberMeTrue() {
        // Arrange
        LoginRequest request = buildRequest();
        request.setRememberMe(Boolean.TRUE);
        UserDO user = buildEnabledUser(0);
        when(loginAttemptManager.isIpRateLimited(CLIENT_IP)).thenReturn(false);
        when(userMapper.getByUserName(USERNAME)).thenReturn(user);
        when(loginAttemptManager.isLocked(USERNAME)).thenReturn(false);
        when(loginAttemptManager.isCaptchaRequired(0)).thenReturn(false);
        when(passwordEncoder.matches(PASSWORD, user.getPasswordHash())).thenReturn(true);
        when(sessionManager.createSession(eq(1L), eq(USERNAME), eq(true))).thenReturn(TOKEN);
        when(sessionManager.getSessionTtl(true)).thenReturn(REMEMEMBER_ME_TTL);

        // Act
        LoginResponse response = authService.login(request, CLIENT_IP);

        // Assert
        assertThat(response.getExpiresIn()).isEqualTo(REMEMEMBER_ME_TTL);
        verify(sessionManager).createSession(1L, USERNAME, true);
    }

    @Test
    @DisplayName("账号不存在：统一返回账号或密码错误（防枚举）")
    void should_throwAccountOrPasswordError_when_userNotFound() {
        // Arrange
        LoginRequest request = buildRequest();
        when(loginAttemptManager.isIpRateLimited(CLIENT_IP)).thenReturn(false);
        when(userMapper.getByUserName(USERNAME)).thenReturn(null);

        // Act + Assert
        assertThatThrownBy(() -> authService.login(request, CLIENT_IP))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException biz = (BusinessException) ex;
                    assertThat(biz.getResponseCode())
                            .isEqualTo(ResponseCodeEnum.ACCOUNT_OR_PASSWORD_ERROR);
                });
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("密码错误：累计失败次数并返回账号或密码错误")
    void should_incrementFailCount_when_passwordWrong() {
        // Arrange
        LoginRequest request = buildRequest();
        UserDO user = buildEnabledUser(2);
        when(loginAttemptManager.isIpRateLimited(CLIENT_IP)).thenReturn(false);
        when(userMapper.getByUserName(USERNAME)).thenReturn(user);
        when(loginAttemptManager.isLocked(USERNAME)).thenReturn(false);
        when(loginAttemptManager.isCaptchaRequired(2)).thenReturn(false);
        when(passwordEncoder.matches(PASSWORD, user.getPasswordHash())).thenReturn(false);
        when(loginAttemptManager.applyLockIfExceeded(USERNAME, 3)).thenReturn(false);

        // Act + Assert
        assertThatThrownBy(() -> authService.login(request, CLIENT_IP))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException biz = (BusinessException) ex;
                    assertThat(biz.getResponseCode())
                            .isEqualTo(ResponseCodeEnum.ACCOUNT_OR_PASSWORD_ERROR);
                });
        verify(userMapper).updateFailStatus(eq(1L), eq(3), eq(null));
        verify(userMapper, never()).resetFailStatus(anyLong());
    }

    @Test
    @DisplayName("暴力破解锁定：失败次数达上限后锁定")
    void should_lockAccount_when_failCountExceedsMax() {
        // Arrange
        LoginRequest request = buildRequest();
        UserDO user = buildEnabledUser(4);
        when(loginAttemptManager.isIpRateLimited(CLIENT_IP)).thenReturn(false);
        when(userMapper.getByUserName(USERNAME)).thenReturn(user);
        when(loginAttemptManager.isLocked(USERNAME)).thenReturn(false);
        when(loginAttemptManager.isCaptchaRequired(4)).thenReturn(false);
        when(passwordEncoder.matches(PASSWORD, user.getPasswordHash())).thenReturn(false);
        when(loginAttemptManager.applyLockIfExceeded(USERNAME, 5)).thenReturn(true);
        when(loginAttemptManager.getLockDurationSeconds()).thenReturn(900L);
        when(loginAttemptManager.buildLockedTip(USERNAME)).thenReturn("账号已锁定，请15分钟后重试");

        // Act + Assert
        assertThatThrownBy(() -> authService.login(request, CLIENT_IP))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException biz = (BusinessException) ex;
                    assertThat(biz.getResponseCode()).isEqualTo(ResponseCodeEnum.ACCOUNT_LOCKED);
                    assertThat(biz.getUserTip()).contains("锁定");
                });
        verify(userMapper).updateFailStatus(eq(1L), eq(5), any());
    }

    @Test
    @DisplayName("账号已锁定：直接拒绝登录")
    void should_throwLocked_when_accountAlreadyLocked() {
        // Arrange
        LoginRequest request = buildRequest();
        UserDO user = buildEnabledUser(5);
        when(loginAttemptManager.isIpRateLimited(CLIENT_IP)).thenReturn(false);
        when(userMapper.getByUserName(USERNAME)).thenReturn(user);
        when(loginAttemptManager.isLocked(USERNAME)).thenReturn(true);
        when(loginAttemptManager.buildLockedTip(USERNAME)).thenReturn("账号已锁定，请15分钟后重试");

        // Act + Assert
        assertThatThrownBy(() -> authService.login(request, CLIENT_IP))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException biz = (BusinessException) ex;
                    assertThat(biz.getResponseCode()).isEqualTo(ResponseCodeEnum.ACCOUNT_LOCKED);
                });
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("账号禁用：返回账号已禁用")
    void should_throwDisabled_when_accountDisabled() {
        // Arrange
        LoginRequest request = buildRequest();
        UserDO user = buildEnabledUser(0);
        user.setStatus(0);
        when(loginAttemptManager.isIpRateLimited(CLIENT_IP)).thenReturn(false);
        when(userMapper.getByUserName(USERNAME)).thenReturn(user);
        when(loginAttemptManager.isLocked(USERNAME)).thenReturn(false);

        // Act + Assert
        assertThatThrownBy(() -> authService.login(request, CLIENT_IP))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException biz = (BusinessException) ex;
                    assertThat(biz.getResponseCode()).isEqualTo(ResponseCodeEnum.ACCOUNT_DISABLED);
                });
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("IP 限流：返回限流提示")
    void should_throwIpRateLimited_when_ipExceedsLimit() {
        // Arrange
        LoginRequest request = buildRequest();
        when(loginAttemptManager.isIpRateLimited(CLIENT_IP)).thenReturn(true);

        // Act + Assert
        assertThatThrownBy(() -> authService.login(request, CLIENT_IP))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException biz = (BusinessException) ex;
                    assertThat(biz.getResponseCode()).isEqualTo(ResponseCodeEnum.IP_RATE_LIMITED);
                });
        verify(userMapper, never()).getByUserName(anyString());
    }

    @Test
    @DisplayName("需要验证码：失败次数超阈值且未提供验证码")
    void should_throwCaptchaRequired_when_captchaMissing() {
        // Arrange
        LoginRequest request = buildRequest();
        UserDO user = buildEnabledUser(3);
        when(loginAttemptManager.isIpRateLimited(CLIENT_IP)).thenReturn(false);
        when(userMapper.getByUserName(USERNAME)).thenReturn(user);
        when(loginAttemptManager.isLocked(USERNAME)).thenReturn(false);
        when(loginAttemptManager.isCaptchaRequired(3)).thenReturn(true);

        // Act + Assert
        assertThatThrownBy(() -> authService.login(request, CLIENT_IP))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException biz = (BusinessException) ex;
                    assertThat(biz.getResponseCode()).isEqualTo(ResponseCodeEnum.CAPTCHA_REQUIRED);
                });
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("验证码错误：返回验证码错误")
    void should_throwCaptchaError_when_captchaInvalid() {
        // Arrange
        LoginRequest request = buildRequest();
        request.setCaptcha("0000");
        request.setCaptchaId("cap-id");
        UserDO user = buildEnabledUser(3);
        when(loginAttemptManager.isIpRateLimited(CLIENT_IP)).thenReturn(false);
        when(userMapper.getByUserName(USERNAME)).thenReturn(user);
        when(loginAttemptManager.isLocked(USERNAME)).thenReturn(false);
        when(loginAttemptManager.isCaptchaRequired(3)).thenReturn(true);
        when(captchaManager.validate("cap-id", "0000")).thenReturn(false);

        // Act + Assert
        assertThatThrownBy(() -> authService.login(request, CLIENT_IP))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> {
                    BusinessException biz = (BusinessException) ex;
                    assertThat(biz.getResponseCode()).isEqualTo(ResponseCodeEnum.CAPTCHA_ERROR);
                });
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("验证码正确且密码正确：登录成功")
    void should_loginSuccess_when_captchaValidAndPasswordCorrect() {
        // Arrange
        LoginRequest request = buildRequest();
        request.setCaptcha("1234");
        request.setCaptchaId("cap-id");
        UserDO user = buildEnabledUser(3);
        when(loginAttemptManager.isIpRateLimited(CLIENT_IP)).thenReturn(false);
        when(userMapper.getByUserName(USERNAME)).thenReturn(user);
        when(loginAttemptManager.isLocked(USERNAME)).thenReturn(false);
        when(loginAttemptManager.isCaptchaRequired(3)).thenReturn(true);
        when(captchaManager.validate("cap-id", "1234")).thenReturn(true);
        when(passwordEncoder.matches(PASSWORD, user.getPasswordHash())).thenReturn(true);
        when(sessionManager.createSession(1L, USERNAME, false)).thenReturn(TOKEN);
        when(sessionManager.getSessionTtl(false)).thenReturn(SESSION_TTL);

        // Act
        LoginResponse response = authService.login(request, CLIENT_IP);

        // Assert
        assertThat(response.getToken()).isEqualTo(TOKEN);
        verify(userMapper).resetFailStatus(1L);
    }

    // ==================== logout 测试 ====================

    @Test
    @DisplayName("退出登录：注销会话")
    void should_invalidateSession_when_logout() {
        // Act
        authService.logout(TOKEN);

        // Assert
        verify(sessionManager).invalidate(TOKEN);
    }

    @Test
    @DisplayName("退出登录：令牌为空时不调用注销")
    void should_notInvalidate_when_tokenBlank() {
        // Act
        authService.logout("");

        // Assert
        verify(sessionManager, never()).invalidate(anyString());
    }

    @Test
    @DisplayName("退出登录：令牌为 null 时不调用注销")
    void should_notInvalidate_when_tokenNull() {
        // Act
        authService.logout(null);

        // Assert
        verify(sessionManager, never()).invalidate(anyString());
    }
}
