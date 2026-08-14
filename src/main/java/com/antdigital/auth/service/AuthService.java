package com.antdigital.auth.service;

import com.antdigital.auth.model.dto.LoginRequest;
import com.antdigital.auth.model.dto.LoginResponse;

/**
 * 认证服务接口。
 */
public interface AuthService {

    /**
     * 用户登录。
     *
     * @param request  登录请求
     * @param clientIp 客户端IP
     * @return 登录响应（含会话令牌）
     */
    LoginResponse login(LoginRequest request, String clientIp);

    /**
     * 退出登录。
     *
     * @param token 会话令牌
     */
    void logout(String token);
}
